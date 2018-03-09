package ReplicaManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;

import Misc.Message;
import Misc.ObjectListMessage;
import Misc.Message.Type;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class ReplicaConnection implements Runnable {
	public enum State{LAUNCHED, INTEGRATED, PRIMARY, BACKUP}
	private State m_state;
	private boolean running = true;
	private Socket m_socket;
	private ObjectInputStream m_inStream;
	private ObjectOutputStream m_outStream;
	private ArrayList<StandardMessage> m_standardMessages;
	private ArrayList<ObjectMessage> m_objectMessages;
	private ArrayList<ObjectListMessage> m_objectListMessages;
	
	public ReplicaConnection(Socket socket){
		System.out.println("Connected to: " + socket.getPort());
		m_state = State.INTEGRATED;
		m_socket = socket;
		try {
			m_outStream = new ObjectOutputStream(socket.getOutputStream());
			m_inStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
		m_standardMessages = new ArrayList<>();
		m_objectMessages = new ArrayList<>();
		m_objectListMessages = new ArrayList<>();
	}
	
	public void send(Message message){
		try {
			if(message.getType() == Type.STANDARDMESSAGE)
				System.out.println("Sent: " + ((StandardMessage)message).getMessage());
			
			m_outStream.flush();
			m_outStream.writeObject(message);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public ObjectMessage getObjectMessage(){
		if(m_objectMessages.size() > 0 && m_objectMessages.get(0).getObject() != null)
			return m_objectMessages.remove(0);
		return null;
	}
	
	public ObjectListMessage getObjectListMessage(){
		if(m_objectListMessages.size() > 0 && m_objectListMessages.get(0).getObjects() != null)
			return m_objectListMessages.remove(0);
		return null;
	}
	
	public StandardMessage getMessage(){
		if(m_standardMessages.size() > 0 && m_standardMessages.get(0).getMessage() != null){
			return m_standardMessages.remove(0);
		}
		return null;
	}
	
	public void recieveMessages(){
		try {
			Message message = (Message) m_inStream.readObject();
			switch (message.getType()) {
			case OBJECTMESSAGE:
				m_objectMessages.add((ObjectMessage) message);
				break;
			case STANDARDMESSAGE:
				m_standardMessages.add((StandardMessage) message);
				System.out.println("Recieved: " + ((StandardMessage) message).getMessage());
				break;
			case OBJECTLISTMESSAGE:

				break;
			default:
				break;
			}
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (Exception e) {
			// Server crashed
			e.printStackTrace();
		}
	}
	
	public void setState(State state) {
		m_state = state;
	}
	
	public State getState(){
		return m_state;
	}

	@Override
	public void run() {
		while(running) {
			recieveMessages();
		}
	}
}
