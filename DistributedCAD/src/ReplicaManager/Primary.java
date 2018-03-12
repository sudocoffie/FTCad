package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.LinkedBlockingQueue;

import DCAD.GObject;
import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectListMessage;
import Misc.ObjectMessage;
import Misc.ReplyMessage;
import Misc.StandardMessage;

public class Primary implements Runnable {
	private ArrayList<ReplicaConnection> m_connections;
	private DatagramSocket m_socket;
	private ArrayList<GObject> m_objects;
	private ArrayList<String> m_clients;
	private InetSocketAddress m_address;
	private LinkedBlockingQueue<ReplyMessage> m_replyMessages;

	public Primary(ArrayList<ReplicaConnection> connections, DatagramSocket socket, InetSocketAddress address) {
		m_objects = new ArrayList<>();
		m_clients = new ArrayList<>();
		init(connections, socket, address);
	}

	public Primary(ArrayList<ReplicaConnection> connections, DatagramSocket socket, InetSocketAddress address,
			ArrayList<GObject> objects, ArrayList<String> clients) {
		m_objects = objects;
		m_clients = clients;
		init(connections, socket, address);
	}

	private void init(ArrayList<ReplicaConnection> connections, DatagramSocket socket, InetSocketAddress address) {
		m_connections = connections;
		m_socket = socket;
		m_address = address;

		m_replyMessages = new LinkedBlockingQueue<>();

		// Continually sends message to frontend to inform who's primary
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					StandardMessage welcome = new StandardMessage("primary");
					sendMessage(MessageConvertion.objectToBytes(welcome), m_socket, m_address.getAddress(),
							m_address.getPort());
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		// Integrates launched replicas
		new Thread(new Runnable() {
			public void run() {
				while (true) {
					for (ReplicaConnection c : m_connections) {
						StandardMessage message;
						if ((message = c.getMessage()) != null && message.getMessage().startsWith("election")) {
							c.send(new StandardMessage("election won " + ReplicaManager.ID));
							if (m_objects.size() > 0) {
								GObject[] objects = new GObject[m_objects.size()];
								for (int i = 0; i < m_objects.size(); i++)
									objects[i] = m_objects.get(i);
								c.send(new ObjectListMessage(null, -1, objects));
							}
							for (String s : m_clients)
								c.send(new StandardMessage(null, -1, s));
						}
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();

		new Thread(this).start();
	}

	@Override
	public void run() {
		while (true) {
			byte[] buf = new byte[1024];
			DatagramPacket packet = new DatagramPacket(buf, buf.length);
			try {
				m_socket.receive(packet);
				Message message = (Message) MessageConvertion.bytesToObject(packet.getData());
				switch (message.getType()) {
				case STANDARDMESSAGE:
					StandardMessage standardMessage = (StandardMessage) message;
					System.out.println(standardMessage.getMessage());
					if (standardMessage.getMessage().startsWith("join")) {
						// Adds a new client and updates backups
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
							sendToAllBackups(new StandardMessage(null, -1, client));
							if (m_objects.size() > 0) {
								GObject[] objects = new GObject[m_objects.size()];
								for (int i = 0; i < m_objects.size(); i++)
									objects[i] = m_objects.get(i);
								ObjectListMessage objectListMessage = new ObjectListMessage(message.getAddress(),
										message.getPort(), objects);
								ArrayList<String> clientReply = new ArrayList<>();
								clientReply.add(client);
								sendUntilReply(objectListMessage, clientReply);
							}
						}
					} else if (standardMessage.getMessage().startsWith("remove")) {
						removeMessage(standardMessage);
					}
					reply(message);
					break;
				case OBJECTMESSAGE:
					// Adds object if new and updates backups
					boolean addObject = true;
					ObjectMessage objMessage = (ObjectMessage) message;
					for (GObject object : m_objects) {
						if (object.getId().equals(objMessage.getId())) {
							addObject = false;
						}
					}
					if (addObject) {
						GObject object = objMessage.getObject();
						object.setId(objMessage.getId());
						m_objects.add(object);
						// Sends to all backups and clients
						ObjectMessage sendMessage = new ObjectMessage(null, -1, object);
						sendToAllBackups(sendMessage);
						sendUntilReply(message, m_clients);
					}
					reply(message);
					break;
				case REPLYMESSAGE:
					ReplyMessage replyMessage = (ReplyMessage) message;
					m_replyMessages.offer(replyMessage);
					break;
				default:
					break;
				}
			} catch (PortUnreachableException e) {

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void sendUntilReply(final Message msg, final ArrayList<String> clients) {
		new Thread(new Runnable() {
			public void run() {
				ArrayList<String> notReplied = new ArrayList<>();
				notReplied.addAll(clients);
				Message message = msg;
				int dcCounter = 0;
				while (notReplied.size() > 0) {
					// Sends message to all selected clients
					for (String client : notReplied) {
						try {
							message.setAddress(InetAddress.getByName(client.split(" ")[1].split("/")[0]));
						} catch (UnknownHostException e) {
							e.printStackTrace();
						}
						message.setPort(Integer.parseInt(client.split(" ")[2]));
						sendMessage(MessageConvertion.objectToBytes(message), m_socket, m_address.getAddress(),
								m_address.getPort());
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					// Checks for reply and removes clients that replied from list
					ArrayList<ReplyMessage> addToList = new ArrayList<>();
					ReplyMessage reply;
					while ((reply = m_replyMessages.poll()) != null) {
						if (reply.getReplyId().equals(message.getId())) {
							String remove = null;
							dcCounter = 0;
							for (String client : notReplied) {
								if (client.split(" ")[1].equals(message.getAddress().toString())
										&& Integer.parseInt(client.split(" ")[2]) == message.getPort()) {
									remove = client;
								}
							}
							notReplied.remove(remove);
						} else
							addToList.add(reply);

					}
					m_replyMessages.addAll(addToList);
					// Checks if clients is offline and updates backups
					dcCounter++;
					if (dcCounter > 5 && clients.size() > notReplied.size()) {
						m_clients.removeAll(notReplied);
						for (String m : notReplied) {
							StandardMessage sendMessage = new StandardMessage("removeClient " + m);
							sendToAllBackups(sendMessage);
						}
						notReplied.clear();
					}
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}).start();
	}

	private void reply(Message message) {
		ReplyMessage reply = new ReplyMessage(message.getAddress(), message.getPort(), message.getId());
		sendMessage(MessageConvertion.objectToBytes(reply), m_socket, m_address.getAddress(), m_address.getPort());
	}

	private void sendToAllBackups(Message message) {
		for (ReplicaConnection searchForBackup : m_connections) {
			if (searchForBackup.getState() == ReplicaConnection.State.BACKUP) {
				searchForBackup.send(message);
			}
		}
	}

	public void sendMessage(byte[] message, DatagramSocket socket, InetAddress address, int port) {
		byte[] buf = message;
		DatagramPacket m_packet;
		try {
			m_packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(m_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void removeMessage(StandardMessage message) {
		GObject remove = null;
		for (GObject o : m_objects) {
			if (o.getId().toString().equals(message.getMessage().split(" ")[1]))
				remove = o;
		}
		// Updates backups and clients
		if (remove != null) {
			sendToAllBackups(message);
			sendUntilReply(message, m_clients);
		}
		m_objects.remove(remove);
	}

}
