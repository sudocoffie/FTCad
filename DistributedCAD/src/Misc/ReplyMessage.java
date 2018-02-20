package Misc;

import DCAD.GObject;

public class ReplyMessage extends Message{
	protected String m_replyMessage;
	public ReplyMessage(Type type, String replay) {
		super(type);
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
