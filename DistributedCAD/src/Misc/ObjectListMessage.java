package Misc;

import java.net.InetAddress;
import java.util.ArrayList;

import DCAD.GObject;

public class ObjectListMessage extends Message{
	private GObject[] m_objects;
	
	public ObjectListMessage(InetAddress address, int port, GObject[] objects) {
		super(address, port);
		m_type = Type.OBJECTLISTMESSAGE;
		m_objects = objects;
	}

	public GObject[] getObjects(){
		return m_objects;
	}
}
