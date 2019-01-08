package de.fettesteil.masterserver;

import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

public class Main {

	public static ServerSocket socket;
	public static int port = 2222;
	public static String ADMIN_KEY = "test";
	public static final Long LOGIN_TIMEOUT = 3000L;

	public static List<Client> clientList = new ArrayList<Client>();
	public static List<ChildServer> childServerList = new ArrayList<ChildServer>();

	public static void main(String[] args) {
		try {
			socket = new ServerSocket(port);

			new Thread(new TickThread()).start();
			new Thread(new ClientHandler()).run();
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
