package Misc;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.UUID;

public abstract class Message implements Serializable {
	protected Type m_type;
	protected InetAddress m_address;
	protected int m_port;
	protected UUID m_id;

	public enum Type {
		OBJECTMESSAGE, REPLYMESSAGE, STANDARDMESSAGE, OBJECTLISTMESSAGE
	}

	public Message(InetAddress address, int port) {
		m_type = null;
		m_address = address;
		m_port = port;
		m_id = UUID.randomUUID();
	}

	public InetAddress getAddress() {
		return m_address;
	}

	public int getPort() {
		return m_port;
	}

	public void setAddress(InetAddress address) {
		m_address = address;
	}

	public void setPort(int port) {
		m_port = port;
	}

	public Type getType() {
		return m_type;
	}

	public UUID getId() {
		return m_id;
	}
}
