package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.UUID;

import DCAD.GObject;
import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectMessage;
import Misc.ReplyMessage;
import Misc.StandardMessage;

public class Primary implements Runnable {
	private ArrayList<ReplicaConnection> m_connection = new ArrayList<>();
	private DatagramSocket m_socket;
	private ArrayList<GObject> m_object = new ArrayList<>();
	private ArrayList<String> m_clients = new ArrayList<>();
	private InetSocketAddress m_address;

	public Primary(ArrayList<ReplicaConnection> connections, DatagramSocket socket, InetSocketAddress address) {
		m_connection = connections;
		m_socket = socket;
		m_address = address;
		StandardMessage welcome = new StandardMessage(Message.Type.StandardMessage, "hello");
		sendMessage(MessageConvertion.objectToBytes(welcome), m_socket, m_address.getAddress(), m_address.getPort());
		new Thread(this).start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		while (true) {
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				m_socket.receive(packet);
				Message message = (Message) MessageConvertion.bytesToObject(packet.getData());
				System.out.println(message.getType());
				switch (message.getType()) {
				case StandardMessage:
					StandardMessage standardMessage = (StandardMessage) message;
					System.out.println(standardMessage.getMessage());
					if (standardMessage.getMessage().startsWith("join")) {
						boolean addClient = true;
						for (String client : m_clients) {
							if (standardMessage.getId().toString().equals(client.split(" ")[0])) {
								addClient = false;
							}
						}
						if (addClient) {
							String client = standardMessage.getId().toString() + " " + standardMessage.getAddress()
									+ " " + standardMessage.getPort();
							m_clients.add(client);
							sendToAllBackups(new StandardMessage(Message.Type.StandardMessage, null, -1, client));
							System.out.println("you are connected " + standardMessage.getMessage());
						}
					} else if (standardMessage.getMessage().startsWith("remove")) {
						removeMessage(standardMessage);
						System.out.println("you removed " + standardMessage.getMessage());

					}
					// join ett meddelande "join"
					// remove med ett UUID
					break;
				case ObjectMessage:
					boolean addObject = true;
					ObjectMessage objMessage = (ObjectMessage) message;
					for (GObject object : m_object) {
						if (object.getId().equals(objMessage.getId())) {
							addObject = false;
						}
					}
					if (addObject) {
						GObject object = objMessage.getObject();
						object.setId(objMessage.getId());
						m_object.add(object);
						// send to all backups and clients
						ObjectMessage sendMessage = new ObjectMessage(Message.Type.ObjectMessage, null, -1, object);
						sendToAllBackups(sendMessage);
						sendToClients(sendMessage);
					}
					break;
				case ReplyMessage:
					break;
				default:
					break;
				}
				System.out.println(m_address.getAddress() + " " + m_address.getPort());
				ReplyMessage reply = new ReplyMessage(Message.Type.ReplyMessage, message.getAddress(),
						message.getPort(), message.getId());
				sendMessage(MessageConvertion.objectToBytes(reply), m_socket, m_address.getAddress(),
						m_address.getPort());
				// join
				// stand
				// object
				// removeItem
				// reply
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void sendToAllBackups(Message message) {
		// send backups

		for (ReplicaConnection searchForBackup : m_connection) {
			if (searchForBackup.getState() == ReplicaConnection.State.BACKUP) {
				searchForBackup.send(message);
			}
		}
	}

	private void sendToClients(Message message) {

		for (String client : m_clients) {
			try {
				message.setAddress(InetAddress.getByName(client.split(" ")[1].split("/")[0]));
			} catch (UnknownHostException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			message.setPort(Integer.parseInt(client.split(" ")[2]));
			sendMessage(MessageConvertion.objectToBytes(message), m_socket, m_address.getAddress(),
					m_address.getPort());
		}
	}

	public void sendMessage(byte[] message, DatagramSocket socket, InetAddress address, int port) {
		byte[] buf = message;
		DatagramPacket m_packet;
		try {
			m_packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(m_packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void removeMessage(StandardMessage message) {
		// TODO Auto-generated method stub
		GObject remove = null;
		for (GObject o : m_object) {
			if (o.getId().toString().equals(message.getMessage().split(" ")[1]))
				remove = o;
		}
		if (remove != null) {
			sendToAllBackups(message);
			sendToClients(message);
		}
		m_object.remove(remove);
	}

}
