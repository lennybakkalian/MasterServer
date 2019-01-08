package de.fettesteil.masterserver;

import java.net.ServerSocket;

public class Main {

	public static ServerSocket socket;
	public static int port = 2222;
	public static String ADMIN_KEY = "test";
	
	public static void main(String[] args) {
		try {
			socket = new ServerSocket(port);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
