package Misc;

import java.io.Serializable;
import java.net.SocketAddress;
import java.util.ArrayList;

import DCAD.GObject;

public abstract class Message implements Serializable{
	protected Type m_type;
	protected String m_address;
	protected int m_port;
	public enum Type{
		ObjectMessage,
		ReplyMessage,
		StandardMessage,
		
		
	}
	public Message(Type type, String address, int port){
	m_type = type;	
	m_address = address;
	m_port = port;
	}
	public String getAddress(){
		return m_address;
	}
	public int getPort(){
		return m_port;
	}
	public Type getType(){
		return m_type;
	}

}
