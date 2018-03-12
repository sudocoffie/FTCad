package Misc;

import java.net.InetAddress;

import DCAD.GObject;

public class ObjectMessage extends Message {
	protected GObject m_object;

	public ObjectMessage(InetAddress address, int port, GObject object) {
		super(address, port);
		m_object = object;
		m_type = Type.OBJECTMESSAGE;
	}

	public GObject getObject() {
		return m_object;
	}
}
