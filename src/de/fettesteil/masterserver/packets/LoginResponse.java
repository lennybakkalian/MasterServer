package de.fettesteil.masterserver.packets;

public class LoginResponse extends Packet {

	public static final int LOGIN_SUCCESS = 1, LOGIN_FAILURE = 2;

	public LoginResponse(int loginStatus) {
		super(Packet.LOGINRESPONSE);
		put("status", String.valueOf(loginStatus));
	}

}
