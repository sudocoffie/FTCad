package DCAD;

import java.awt.Color;
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
import java.util.concurrent.LinkedBlockingQueue;

import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class ServerConnection implements Runnable {
	private LinkedList<GObject> m_objectList;
	private LinkedBlockingQueue<Message> m_blockedMessage;
	private DatagramSocket m_socket;
	private String m_address;
	private int m_port;
	private volatile boolean m_online = false;
	public ServerConnection(LinkedList<GObject> objectList) {
		m_objectList = objectList;
	
		m_address = "localhost";
		
		
		m_blockedMessage = new LinkedBlockingQueue<>();
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_port = m_socket.getPort();
		new Thread(this).start();
	}

	public void updateClients(GObject current) {
		ObjectMessage message = new ObjectMessage(Message.Type.ObjectMessage, m_address, m_port, current);
		byte[] bytes = MessageConvertion.objectToBytes(message);
		try {
			DatagramPacket packet = new DatagramPacket(bytes, bytes.length, InetAddress.getByName("localhost"), 20050);
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
					m_objectList.add(drawObject);
					break;
				case StandardMessage:
					StandardMessage message = (StandardMessage) object;
					System.out.println(message.getMessage());
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
