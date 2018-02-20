package Misc;

import java.io.Serializable;

import DCAD.GObject;
import Misc.Message.Type;

public class ObjectMessage extends Message implements Serializable{
	protected GObject m_object; 
	public ObjectMessage(Type type, GObject object) {
		super(type);
		m_object = object;
		// TODO Auto-generated constructor stub
	}
	public GObject getObject(){
		return m_object;
		//göra om GOjject till object 
	}
	
}
