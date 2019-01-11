package de.fettesteil.masterserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.fettesteil.masterserver.packets.LoginResponse;
import de.fettesteil.masterserver.packets.Packet;
import de.fettesteil.masterserver.packets.PingTestPacket;

public class Client implements Runnable {

	private Socket socket;
	private BufferedReader br;
	private PrintWriter pw;
	private boolean connected;
	private UUID uuid;
	private boolean authenticated = false;
	private Long connectedSince;
	private int ping = -1;
	public Long pingSend = 0L;

	private String name, location;
	private boolean isServer = false;

	public Client(Socket socket, UUID uuid) {
		this.socket = socket;
		this.uuid = uuid;
		this.connectedSince = System.currentTimeMillis();
	}

	public void setAsChildServer(String name, String location) {
		isServer = true;
		this.name = name;
		this.location = location;
	}

	public Socket getSocket() {
		return socket;
	}

	public boolean isServer() {
		return isServer;
	}

	public String getName() {
		return name;
	}

	public String getLocation() {
		return location;
	}

	public int getPing() {
		return ping;
	}

	public boolean isAuthenticated() {
		return authenticated;
	}

	public Long getConnectedSince() {
		return connectedSince;
	}

	public UUID getUuid() {
		return uuid;
	}

	public void send(Packet p) {
		if (connected) {
			pw.println(p.getData().toJSONString());
		} else {
			log("Can't send packet because socket is closed");
		}
	}

	@Override
	public void run() {
		try {
			br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			pw = new PrintWriter(socket.getOutputStream(), true);
			connected = true;
			synchronized (this) {
				while (connected) {
					String raw = br.readLine();
					if (raw == null || raw.length() == 0)
						break;
					JSONObject rawObj = (JSONObject) new JSONParser().parse(raw);
					int packetid = Integer.valueOf((String) rawObj.get("packetid"));
					if (packetid != Packet.LOGINPACKET && !authenticated) {
						log("!!! Tried to send packet without permission !!!");
						disconnect();
						break;
					}
					switch (packetid) {
					case Packet.LOGINPACKET:
						String key = (String) rawObj.get("key");
						if (key != null && key.equals((String) Main.config.get("masterkey"))) {
							// check if already loggedi in with this uuid
							String uuid = (String) rawObj.get("uuid");
							if (uuid != null && getByUUID(UUID.fromString(uuid)) != null) {
								// already logged in with this uuid
								send(new LoginResponse(LoginResponse.ALREADY_LOGGED_IN));
							} else {
								authenticated = true;
								send(new LoginResponse(LoginResponse.LOGIN_SUCCESS));
								log("Authenticated!");
								// set to server
								if (rawObj.get("isServer") != null && (boolean) rawObj.get("isServer")) {
									log("Set to ChildServer and change uuid to " + rawObj.get("uuid"));
									this.uuid = UUID.fromString((String) rawObj.get("uuid"));
									setAsChildServer(getSocket().getInetAddress().toString(), "-");
									// register server if not exist
									if (!Client.isInConfig(UUID.fromString(uuid))) {
										log("Registered as new ChildServer!");
										Client.addToConfig(name, socket.getInetAddress().toString(), "",
												UUID.fromString(uuid));
									}
								}
							}
						} else {
							send(new LoginResponse(LoginResponse.LOGIN_FAILURE));
							log("Authentication failure");
						}
						break;
					case Packet.LOGINRESPONSE:
						break;
					case Packet.PINGTEST_SEND:
						send(new PingTestPacket(Packet.PINGTEST_RECV));
						break;
					case Packet.PINGTEST_RECV:
						ping = Math.toIntExact(System.currentTimeMillis() - pingSend);
						break;
					// case Packet.ADD_SERVER:
					// AddServerPacket.process((String) rawObj.get("name"),
					// (String) rawObj.get("key"),
					// (String) rawObj.get("location"), (String)
					// rawObj.get("ip"),
					// Integer.valueOf((String) rawObj.get("port")), this);
					// break;
					default:
						log("PacketID (" + packetid + ") not registered");
					}
					if (!authenticated) {
						// disconnect if sending nonsense and not authenticated
						disconnect();
					}
				}
			}
		} catch (Exception e) {
			// e.printStackTrace();
			disconnect();
		}
	}

	public void disconnect() {
		if (connected)
			try {
				log("Disconnected");
				Main.clientList.remove(this);
				if (!socket.isClosed())
					socket.close();
				connected = false;
			} catch (Exception e) {
				e.printStackTrace();
			}
	}

	public void log(String msg) {
		System.out.println("[" + uuid.toString() + "] " + msg);
	}

	// STATIC METHODS

	public static List<Client> getControllers() {
		List<Client> r = new ArrayList<Client>();
		for (int i = 0; i < Main.clientList.size(); i++)
			if (!Main.clientList.get(i).isServer)
				r.add(Main.clientList.get(i));
		return r;
	}

	public static List<Client> getChildServer() {
		List<Client> r = new ArrayList<Client>();
		for (int i = 0; i < Main.clientList.size(); i++)
			if (Main.clientList.get(i).isServer)
				r.add(Main.clientList.get(i));
		return r;
	}

	public static Client getByUUID(UUID uuid) {
		for (int i = 0; i < Main.clientList.size(); i++)
			if (Main.clientList.get(i).getUuid().toString().equals(uuid.toString()))
				return Main.clientList.get(i);
		return null;
	}

	public static void addToConfig(String name, String address, String location, UUID uuid) {
		JSONArray arr = (JSONArray) Main.config.get("childServer");
		JSONObject o = new JSONObject();
		o.put("name", name);
		o.put("address", address);
		o.put("location", location);
		o.put("uuid", uuid.toString());
		arr.add(o);
		// save back to file
		Main.saveConfig();
	}

	public static boolean isInConfig(UUID uuid) {
		JSONArray arr = (JSONArray) Main.config.get("childServer");
		for (Object o : arr) {
			JSONObject json = (JSONObject) o;
			if (((String) json.get("uuid")).equals(uuid.toString()))
				return true;
		}
		return false;
	}

	public static List<JSONObject> getOfflineClients() {
		List<JSONObject> list = new ArrayList<JSONObject>();
		JSONArray arr = (JSONArray) Main.config.get("childServer");
		for (Object o : arr) {
			JSONObject json = (JSONObject) o;
			// check if online
			boolean offline = true;
			for (Client c : getChildServer()) {
				if (c.getUuid().toString().equals((String) json.get("uuid")))
					offline = false;
			}
			if (offline) {
				list.add(json);
			}

		}
		return list;
	}
}
