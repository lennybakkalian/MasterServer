package de.fettesteil.masterserver;

import java.net.Socket;
import java.util.UUID;

public class ChildServer extends Client {

	private String name;

	public ChildServer(Socket socket, UUID uuid, String name) {
		super(socket, uuid);
		this.name = name;
	}

	public String getName() {
		return name;
	}

}
