package Misc;

import java.net.InetAddress;
import java.util.UUID;

public class ReplyMessage extends Message {
	protected UUID m_replyId;

	public ReplyMessage(InetAddress address, int port, UUID id) {
		super(address, port);
		m_replyId = id;
		m_type = Type.REPLYMESSAGE;
	}

	public UUID getReplyId() {
		return m_replyId;
	}
}
