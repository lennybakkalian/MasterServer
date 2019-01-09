package de.fettesteil.masterserver.packets;

public class BroadcastPacket extends Packet{

	public BroadcastPacket(String title, String msg) {
		super(Packet.BROADCAST);
		put("title", title);
		put("msg", msg);
	}
	
}
