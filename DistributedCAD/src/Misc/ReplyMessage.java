package Misc;

import java.net.InetAddress;
import java.util.UUID;

import DCAD.GObject;

public class ReplyMessage extends Message{
	protected UUID m_replyId;
	public ReplyMessage(Type type, InetAddress address, int port, UUID id) {
		super(type, address, port);
		m_replyId = id;
		// TODO Auto-generated constructor stub
	}
	
	public UUID getReplyId() {
		return m_replyId;
	}
}
