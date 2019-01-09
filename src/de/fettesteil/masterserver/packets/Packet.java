package de.fettesteil.masterserver.packets;

import org.json.simple.JSONObject;

public class Packet {

	public static final int
	LOGINPACKET = 1,
	LOGINRESPONSE = 2,
	NEWPROCESS = 3,
	PROCESSFINFO = 4,
	TERMINATEPROCESS = 5,
	PINGTEST_SEND = 6,
	PINGTEST_RECV = 7,
	ADD_SERVER = 8,
	REMOVE_SERVER = 9;

	private int packetid;
	private JSONObject data;

	public Packet(int packetid) {
		this.packetid = packetid;
		this.data = new JSONObject();
		put("packetid", String.valueOf(packetid));
	}

	public int getPacketid() {
		return packetid;
	}

	public JSONObject getData() {
		return data;
	}

	public void put(Object key, Object value) {
		data.put(key, value);
	}
}
