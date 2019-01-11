package de.fettesteil.masterserver;

import de.fettesteil.masterserver.packets.Packet;
import de.fettesteil.masterserver.packets.PingTestPacket;
import de.fettesteil.masterserver.packets.ServerDataPacket;

public class UpdateThread extends Thread {
	@Override
	public void run() {
		while (!interrupted()) {
			synchronized (this) {
				try {
					Thread.currentThread().sleep(2000);
					for (Client c : Client.getControllers())
						if (c.isAuthenticated()) // only send to authenticated users
							c.send(new ServerDataPacket());

					// ping all authenticated users
					for (Client c : Client.getChildServer())
						if (c.isAuthenticated()) {
							c.pingSend = System.currentTimeMillis();
							c.send(new PingTestPacket(Packet.PINGTEST_SEND));
						}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}
}
