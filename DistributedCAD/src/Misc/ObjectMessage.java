package Misc;

import java.io.Serializable;
import java.net.InetAddress;

import DCAD.GObject;
import Misc.Message.Type;

public class ObjectMessage extends Message{
	protected GObject m_object;
	public ObjectMessage(InetAddress address, int port, GObject object) {
		super(address,port);
		m_object = object;
		m_type = Type.OBJECTMESSAGE;
		// TODO Auto-generated constructor stub
	}
	
	public GObject getObject(){
		return m_object;
		//göra om GOjject till object 
	}
}
