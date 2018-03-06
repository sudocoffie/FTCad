package Misc;

import java.io.Serializable;

import DCAD.GObject;
import Misc.Message.Type;

public class ObjectMessage extends Message{
	protected GObject m_object;
	public ObjectMessage(Type type, String address, int port, GObject object) {
		super(type,address,port);
		m_object = object;
		// TODO Auto-generated constructor stub
	}
	public GObject getObject(){
		return m_object;
		//göra om GOjject till object 
	}
}
