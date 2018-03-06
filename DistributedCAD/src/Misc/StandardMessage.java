package Misc;

import DCAD.GObject;
import Misc.Message.Type;

public class StandardMessage extends Message{
	protected String m_message;
	public StandardMessage(Type type, String address, int port, String message ) {
		super(type, address, port);
		m_message = message;
		// TODO Auto-generated constructor stub
	}
	public String getMessage(){
		return m_message;
		
	}
	
}
