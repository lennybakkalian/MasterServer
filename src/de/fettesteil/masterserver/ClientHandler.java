package de.fettesteil.masterserver;

import java.net.Socket;
import java.util.UUID;

public class ClientHandler implements Runnable {

	@Override
	public void run() {
		while (!Thread.currentThread().interrupted()) {
			synchronized (this) {
				try {
					Socket s = Main.socket.accept();
					Client c = new Client(s, UUID.randomUUID());
					Main.clientList.add(c);
					c.log("Client connected: " + s.getInetAddress());
					new Thread(c).start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
	}

}
