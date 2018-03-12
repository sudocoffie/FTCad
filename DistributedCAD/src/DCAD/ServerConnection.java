package DCAD;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.UUID;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectListMessage;
import Misc.ObjectMessage;
import Misc.ReplyMessage;
import Misc.StandardMessage;

public class ServerConnection implements Runnable {
	private GUI m_gui;
	private LinkedBlockingQueue<ReplyMessage> m_replyMessages;
	private DatagramSocket m_socket;
	private InetAddress m_address;
	private int m_port;
	private boolean m_initiated = false;

	public ServerConnection(GUI gui) {
		m_gui = gui;

		// Reads frontend config
		BufferedReader frontendConfig = null;
		try {
			frontendConfig = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
		} catch (FileNotFoundException e2) {
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

		m_replyMessages = new LinkedBlockingQueue<>();
		try {
			m_socket = new DatagramSocket();
		} catch (SocketException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(this).start();
		// Sends join message to replica
		StandardMessage message = new StandardMessage(InetAddress.getLoopbackAddress(), m_socket.getLocalPort(),
				"join");
		sendUntilResponse(message);
	}

	public void removeObject(GObject remove) {
		StandardMessage message = new StandardMessage(InetAddress.getLoopbackAddress(), m_socket.getLocalPort(),
				"remove " + remove.getId().toString());
		sendUntilResponse(message);
	}

	public void addObject(GObject current) {
		ObjectMessage message = new ObjectMessage(InetAddress.getLoopbackAddress(), m_socket.getLocalPort(), current);
		sendUntilResponse(message);
	}

	private void sendUntilResponse(Message message) {
		// Sends message until a response with that id is received
		UUID id = message.getId();

		boolean recievedResponse = false;
		while (!recievedResponse) {
			try {
				System.out.println(id);
				send(message);
				ReplyMessage response = m_replyMessages.poll(1000, TimeUnit.MILLISECONDS);
				if (response != null && response.getReplyId().equals(id)) {
					recievedResponse = true;
					System.out.println(response.getReplyId());
				}
			} catch (InterruptedException e) {
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
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void reply(Message message) {
		ReplyMessage reply = new ReplyMessage(message.getAddress(), message.getPort(), message.getId());
		send(reply);
	}

	@Override
	public void run() {
		while (true) {
			DatagramPacket packet = new DatagramPacket(new byte[8192], 8192);
			try {
				m_socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}
			Object object = MessageConvertion.bytesToObject(packet.getData());
			// Handles the message depending on type
			if (object != null) {
				switch (((Message) object).getType()) {
				case OBJECTMESSAGE:
					// Adds object if it hasn't already been received and sends reply
					GObject drawObject = ((ObjectMessage) object).getObject();
					boolean add = true;
					for (GObject o : m_gui.getObjects()) {
						if (o.getId().equals(drawObject.getId()))
							add = false;
					}
					if (add)
						m_gui.getObjects().add(drawObject);
					m_gui.repaint();
					reply((Message) object);
					break;
				case STANDARDMESSAGE:
					// Removes object with the specified UUID
					StandardMessage message = (StandardMessage) object;
					if (message.getMessage().startsWith("remove")) {
						GObject remove = null;
						for (GObject o : m_gui.getObjects()) {
							if (o.getId().toString().equals(message.getMessage().split(" ")[1]))
								remove = o;
						}
						m_gui.getObjects().remove(remove);
						m_gui.repaint();
					}
					reply(message);
					System.out.println(message.getMessage());
					break;
				case OBJECTLISTMESSAGE:
					// Adds all objects from server (only happens in the beginning
					ObjectListMessage objectsMessage = (ObjectListMessage) object;
					if (!m_initiated) {
						GObject[] objects = objectsMessage.getObjects();
						for (int i = 0; i < objects.length; i++)
							m_gui.getObjects().add(objects[i]);
						m_gui.repaint();
					}
					reply(objectsMessage);
					break;
				case REPLYMESSAGE:
					System.out.println("reply");
					if (!m_replyMessages.offer((ReplyMessage) object))
						System.err.println("m_blockedMessage full! (ServerConnection.run() switch case)");
					break;
				default:
					System.err.println("\"" + ((Message) object).getType()
							+ "\" is not a valid type (ServerConnection.run() switch case)");
					break;
				}
			} else
				System.out.println("null");

		}
	}
}
