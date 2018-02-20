package Misc;

import java.io.Serializable;
import java.util.ArrayList;

import DCAD.GObject;

public abstract class Message implements Serializable{
	protected Type m_type;
	public enum Type{
		ObjectMessage,
		ReplyMessage,
		StandardMessage,
		
		
	}
	public Message(Type type){
	m_type = type;	
	}
	
	public Type getType(){
		
		
		return m_type;
	}

}
