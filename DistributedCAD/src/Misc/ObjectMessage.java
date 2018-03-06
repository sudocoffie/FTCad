package Misc;

import java.io.Serializable;
import java.net.InetAddress;

import DCAD.GObject;
import Misc.Message.Type;

public class ObjectMessage extends Message{
	protected GObject m_object;
	public ObjectMessage(Type type, InetAddress address, int port, GObject object) {
		super(type,address,port);
		m_object = object;
		// TODO Auto-generated constructor stub
	}
	public GObject getObject(){
		return m_object;
		//g�ra om GOjject till object 
	}
}
