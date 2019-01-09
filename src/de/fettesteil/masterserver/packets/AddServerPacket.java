package de.fettesteil.masterserver.packets;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import de.fettesteil.masterserver.Client;

@Deprecated
public class AddServerPacket {

	public static void process(String name, String key, String location, String ip, int port, Client sender) {

		// check if key is valid

		try {
			// TEST SOCKET
			Socket s = new Socket(ip, port);
			BufferedReader br = new BufferedReader(new InputStreamReader(s.getInputStream()));
			PrintWriter pw = new PrintWriter(s.getOutputStream(), true);
			// send login packet to socket
			pw.println(new LoginPacket(key).getData().toJSONString());
			new Thread(new Runnable() {
				@Override
				public void run() {
					// disconnect socket if no packet after 5 seconds
					new Thread(new Runnable() {
						@Override
						public void run() {
							try {
								Thread.currentThread().sleep(5000);
								if (!s.isClosed())
									s.close();
							} catch (Exception e) {
								e.printStackTrace();
							}
						}
					}).start();
					try {
						JSONObject o = (JSONObject) new JSONParser().parse(br.readLine());
						if (Integer.valueOf((String) o.get("packetid")) == Packet.LOGINRESPONSE) {
							int status = Integer.valueOf((String) o.get("status"));
							if (status == LoginResponse.LOGIN_SUCCESS) {
								sender.send(
										new BroadcastPacket("Erfolgreich", "Server wurde erfolgreich registriert!"));
							} else {
								sender.send(new BroadcastPacket("Fehler", "Ungültiger Key"));
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
						sender.send(new BroadcastPacket("Fehler",
								"Server konnte nicht hinzugefügt werden... " + e.getMessage()));
					}
				}
			}).start();
		} catch (Exception e) {
			e.printStackTrace();
			sender.send(new BroadcastPacket("Exception",
					"Fehler beim Verbindungsaufbau zu " + ip + ":" + port + " (" + e.getMessage() + ")"));
		}

	}

}
