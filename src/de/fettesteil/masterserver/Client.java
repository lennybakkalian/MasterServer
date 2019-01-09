package de.fettesteil.masterserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.fettesteil.masterserver.packets.AddServerPacket;
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
							authenticated = true;
							send(new LoginResponse(LoginResponse.LOGIN_SUCCESS));
							log("Authenticated!");
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

}
