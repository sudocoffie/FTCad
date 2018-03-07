package DCAD;

import java.awt.Color;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectMessage;
import Misc.ReplyMessage;
import Misc.StandardMessage;

public class ServerConnection implements Runnable {
	private LinkedList<GObject> m_objectList;
	private LinkedBlockingQueue<Message> m_blockedMessage;
	private DatagramSocket m_socket;
	private InetAddress m_address;
	private int m_port;
	public ServerConnection(LinkedList<GObject> objectList) {
		m_objectList = objectList;
		
		BufferedReader frontendConfig = null;
		try {
			frontendConfig = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		String line;
		try {
			while ((line = frontendConfig.readLine()) != null) {
				m_address = InetAddress.getByName(line.split(" ")[0]);
				m_port = Integer.parseInt(line.split(" ")[2]);
			}
			frontendConfig.close();
		} catch (NumberFormatException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		m_blockedMessage = new LinkedBlockingQueue<>();
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(this).start();
		StandardMessage message = new StandardMessage(Message.Type.StandardMessage, m_socket.getLocalAddress(), m_socket.getLocalPort(), "join");
		sendUntilResponse(message);
	}
	
	public void removeObject(GObject remove) {
		StandardMessage message = new StandardMessage(Message.Type.StandardMessage, m_socket.getLocalAddress(), m_socket.getLocalPort(), "remove " + remove.getId().toString());
		sendUntilResponse(message);
	}

	public void addObject(GObject current) {
		ObjectMessage message = new ObjectMessage(Message.Type.ObjectMessage, m_socket.getLocalAddress(), m_socket.getLocalPort(), current);
		sendUntilResponse(message);
	}

	private void sendUntilResponse(Message message) {
		UUID id = message.getId();
		System.out.println(id);
		boolean recievedResponse = false;
		while(!recievedResponse) {
			try {
				send(message);
				ReplyMessage response = (ReplyMessage) m_blockedMessage.poll(1000, TimeUnit.MILLISECONDS);
				if(response != null && response.getReplyId().equals(id)) {
					recievedResponse = true;
					System.out.println(response.getReplyId());
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	private void send(Message message) {
		byte[] bytes = MessageConvertion.objectToBytes(message);
		try {
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, m_address, m_port);
			m_socket.send(packet);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[1024], 1024);
			try {
				m_socket.receive(packet);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Object object = MessageConvertion.bytesToObject(packet.getData());
			if (object != null) {
				switch (((Message) object).getType()) {
				case ObjectMessage:
					GObject drawObject = ((ObjectMessage) object).getObject();
					boolean add = true;
					for(GObject o : m_objectList) {
						if(o.getId().equals(drawObject.getId()))
							add = false;
					}
					if(add)
						m_objectList.add(drawObject);
					break;
				case StandardMessage:
					StandardMessage message = (StandardMessage) object;
					if(message.getMessage().startsWith("remove")) {
						GObject remove = null;
						for(GObject o : m_objectList) {
							if(o.getId().toString().equals(message.getMessage().split(" ")[1]))
								remove = o;
						}
						m_objectList.remove(remove);
					}
					System.out.println(message.getMessage());
					break;
				case ReplyMessage:
					System.out.println("reply");
					if(!m_blockedMessage.offer((ReplyMessage)object))
						System.err.println("m_blockedMessage full! (ServerConnection.run() switch case)");
					break;
				default:
					System.err.println("\"" + ((Message) object).getType()
							+ "\" is not a valid type (ServerConnection.run() switch case)");
					break;
				}
			}else 
				System.out.println("null");

		}
	}
}
