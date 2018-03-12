package Misc;

import java.net.InetAddress;

public class StandardMessage extends Message {
	protected String m_message;

	public StandardMessage(InetAddress address, int port, String message) {
		super(address, port);
		m_message = message;
		m_type = Type.STANDARDMESSAGE;
	}

	public StandardMessage(String message) {
		super(null, -1);
		m_message = message;
		m_type = Type.STANDARDMESSAGE;
	}

	public String getMessage() {
		return m_message;
	}
}
