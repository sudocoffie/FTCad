package Misc;

import java.io.Serializable;
import java.net.InetAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.UUID;

import DCAD.GObject;

public abstract class Message implements Serializable {
	protected Type m_type;
	protected InetAddress m_address;
	protected int m_port;
	protected UUID m_id;

	public enum Type {
		ObjectMessage, ReplyMessage, StandardMessage
	}

	public Message(Type type, InetAddress address, int port) {
		m_type = type;
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

	public Type getType() {
		return m_type;
	}

	public UUID getId() {
		return m_id;
	}
}
