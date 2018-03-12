package ReplicaManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

import Misc.Message;
import Misc.ObjectListMessage;
import Misc.Message.Type;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class ReplicaConnection implements Runnable {
	public enum State {
		LAUNCHED, INTEGRATED, PRIMARY, BACKUP
	}

	private State m_state;
	private volatile boolean running = true;
	private Socket m_socket;
	private ObjectInputStream m_inStream;
	private ObjectOutputStream m_outStream;
	private ArrayList<StandardMessage> m_standardMessages;
	private ArrayList<ObjectMessage> m_objectMessages;
	private ArrayList<ObjectListMessage> m_objectListMessages;
	private int m_id;

	public ReplicaConnection(Socket socket, int id) {
		m_id = id;
		m_socket = socket;
		initStreams();
		m_standardMessages = new ArrayList<>();
		m_objectMessages = new ArrayList<>();
		m_objectListMessages = new ArrayList<>();
		m_state = State.INTEGRATED;
	}

	private void initStreams() {
		try {
			m_outStream = new ObjectOutputStream(m_socket.getOutputStream());
			m_inStream = new ObjectInputStream(m_socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void send(Message message) {
		try {
			if (running) {
				if (message.getType() == Type.STANDARDMESSAGE)
					System.out.println(m_id + " Sent: " + ((StandardMessage) message).getMessage());
				m_outStream.flush();
				m_outStream.writeObject(message);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized ObjectMessage getObjectMessage() {
		try {
			if (m_objectMessages.size() > 0 && m_objectMessages.get(0).getObject() != null)
				return m_objectMessages.remove(0);
		} catch (NullPointerException e) {
			m_standardMessages.remove(0);
		}
		return null;
	}

	public synchronized ObjectListMessage getObjectListMessage() {
		try {
			if (m_objectListMessages.size() > 0 && m_objectListMessages.get(0).getObjects() != null)
				return m_objectListMessages.remove(0);
		} catch (NullPointerException e) {
			m_standardMessages.remove(0);
		}
		return null;
	}

	public synchronized StandardMessage getMessage() {
		try {
			if (m_standardMessages.size() > 0 && m_standardMessages.get(0).getMessage() != null) {
				return m_standardMessages.remove(0);
			}
		} catch (NullPointerException e) {
			m_standardMessages.remove(0);
		}
		return null;
	}

	@Override
	public void run() {
		while (true) {
			recieveMessages();
		}
	}

	public void recieveMessages() {
		// Receives messages and adds them to appropriate list
		try {
			if (running) {
				Message message = (Message) m_inStream.readObject();
				switch (message.getType()) {
				case OBJECTMESSAGE:
					m_objectMessages.add((ObjectMessage) message);
					break;
				case STANDARDMESSAGE:
					StandardMessage m = (StandardMessage) message;
					m_standardMessages.add(m);
					System.out.println(m_id + " Recieved: " + ((StandardMessage) message).getMessage());
					break;
				case OBJECTLISTMESSAGE:
					break;
				default:
					break;
				}
			} else {
				Thread.sleep(100);
			}

		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (SocketException e) {
			// Server crashed
			System.out.println("Lost connection to " + getId());
			m_state = State.LAUNCHED;
			running = false;
		} catch (Exception e) {

		}
	}

	public void acceptSocketConnection() {
		ServerSocket serverSocket = null;
		// Closes the streams if open
		if (m_outStream != null) {
			try {
				m_inStream.close();
				m_outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			serverSocket = new ServerSocket(m_socket.getLocalPort());
			m_socket = serverSocket.accept();
			System.out.println("Accepted from " + getId());
			initStreams();
			running = true;
			m_state = State.BACKUP;
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			serverSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void connectToSocket() {
		SocketAddress address = m_socket.getRemoteSocketAddress();
		if (m_outStream != null) {
			try {
				m_inStream.close();
				m_outStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		boolean connected = false;
		while (!connected) {
			try {
				m_socket = new Socket();
				m_socket.connect(address);
				initStreams();
				running = true;
				send(new StandardMessage(m_id + ""));
				connected = true;
				m_state = State.BACKUP;
				System.out.println("Connected to " + getId());
			} catch (IOException e) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
		}
	}

	public synchronized boolean isRunning() {
		return running;
	}

	public void setState(State state) {
		m_state = state;
	}

	public State getState() {
		return m_state;
	}

	public void setId(int id) {
		m_id = id;
	}

	public int getId() {
		return m_id;
	}
}
