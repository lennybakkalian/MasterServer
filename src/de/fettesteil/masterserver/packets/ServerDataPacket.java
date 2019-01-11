package de.fettesteil.masterserver.packets;

import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import de.fettesteil.masterserver.Client;
import de.fettesteil.masterserver.Main;

public class ServerDataPacket extends Packet {

	public ServerDataPacket() {
		super(Packet.SEND_SERVERDATA);
		JSONArray servers = new JSONArray();

		// TODO: add offline server

		for (int i = 0; i < Main.clientList.size(); i++) {
			Client c = Main.clientList.get(i);
			if (c.isServer()) {
				JSONObject s = new JSONObject();
				s.put("uuid", c.getUuid().toString());
				s.put("name", c.getName());
				s.put("location", c.getLocation());
				s.put("ping", String.valueOf(c.getPing()));
				s.put("address", c.getSocket().getInetAddress().toString());
				s.put("online", true);
				servers.add(s);
			}
		}

		List<JSONObject> list = Client.getOfflineClients();
		for (int i = 0; i < list.size(); i++) {
			JSONObject s = list.get(i);
			s.put("online", false);
			servers.add(s);
		}

		put("servers", servers);
	}

}
