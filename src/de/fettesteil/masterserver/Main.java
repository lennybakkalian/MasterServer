package de.fettesteil.masterserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class Main {

	public static ServerSocket socket;
	public static int port = 2222;
	public static final Long LOGIN_TIMEOUT = 3000L;

	public static List<Client> clientList = new ArrayList<Client>();
	public static List<Client> childServerList = new ArrayList<Client>();

	public static JSONObject config;

	public static void main(String[] args) {
		try {
			File configFile = new File("masterserver.json");
			if (configFile.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(configFile));
				config = (JSONObject) new JSONParser().parse(br.readLine());
				br.close();
				System.out.println("Config Loaded!");
			} else {
				config = new JSONObject();
				config.put("childServer", new JSONArray());
				// TODO: masterkey random generation
				config.put("masterkey", "test");
				FileWriter fw = new FileWriter(configFile);
				fw.write(config.toJSONString());
				fw.close();
				System.out.println("config created");
			}

			socket = new ServerSocket(port);

			new Thread(new TickThread()).start();
			new Thread(new ClientHandler()).run();

		} catch (Exception e) {
			e.printStackTrace();
			System.exit(0);
		}
	}
}
