package Misc;

import DCAD.GObject;

public class ReplyMessage extends Message{
	protected String m_replyMessage;
	public ReplyMessage(Type type,String address, int port, String replay) {
		super(type, address, port);
		m_replyMessage = replay;
		// TODO Auto-generated constructor stub
	}
	
	public String getReplyMessage(){
		return m_replyMessage;
	}
	
	//skoll meddelenad e
	// objectmeddeland 
	//

}
