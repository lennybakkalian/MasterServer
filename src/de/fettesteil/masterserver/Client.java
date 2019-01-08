package de.fettesteil.masterserver;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.UUID;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.fettesteil.masterserver.packets.Packet;

public class Client implements Runnable {

	private Socket socket;
	private BufferedReader br;
	private PrintWriter pw;
	private boolean connected;
	private UUID uuid;

	public Client(Socket socket, UUID uuid) {
		this.socket = socket;
		this.uuid = uuid;
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
			synchronized (this) {
				while (connected) {
					String raw = br.readLine();
					if (raw == null || raw.length() == 0)
						break;
					JSONObject rawObj = (JSONObject) new JSONParser().parse(raw);
					int packetid = Integer.valueOf((String) rawObj.get("packetid"));
					switch (packetid) {
					case Packet.LOGINPACKET:
						break;
					case Packet.LOGINRESPONSE:
						break;
					default:
						log("PacketID (" + packetid + ") not registered");
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			disconnect();
		}
	}

	public void disconnect() {
		try {
			log("Disconnected");
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
