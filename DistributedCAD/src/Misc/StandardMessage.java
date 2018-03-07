package Misc;

import java.net.InetAddress;

import DCAD.GObject;
import Misc.Message.Type;

public class StandardMessage extends Message{
	protected String m_message;
	public StandardMessage(Type type, InetAddress address, int port, String message ) {
		super(type, address, port);
		m_message = message;
		// TODO Auto-generated constructor stub
	}
	
	public StandardMessage(Type type, String message) {
		super(type, null, -1);
		m_message = message;
	}
	
	public String getMessage(){
		return m_message;
	}
}
