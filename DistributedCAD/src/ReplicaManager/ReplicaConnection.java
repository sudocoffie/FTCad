package ReplicaManager;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;

import Misc.Message;
import Misc.Message.Type;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class ReplicaConnection {
	public enum State{LAUNCHED, INTEGRATED, PRIMARY, BACKUP}
	private State m_state;
	private Socket m_socket;
	private ObjectInputStream m_inStream;
	private ObjectOutputStream m_outStream;
	private ArrayList<StandardMessage> m_standardMessages;
	private ArrayList<ObjectMessage> m_objectMessages;
	
	public ReplicaConnection(Socket socket){
		System.out.println("Connected to: " + socket.getPort());
		m_state = State.INTEGRATED;
		m_socket = socket;
		try {
			m_outStream = new ObjectOutputStream(socket.getOutputStream());
			m_inStream = new ObjectInputStream(socket.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		m_standardMessages = new ArrayList<>();
		m_objectMessages = new ArrayList<>();
		
		new Thread(new Runnable(){
				public void run(){
					recieveMessages();
				}}).start();;
	}
	
	public void send(Message message){
		try {
			if(message.getType() == Type.StandardMessage)
				System.out.println("Sent: " + ((StandardMessage)message).getMessage());
			m_outStream.flush();
			m_outStream.writeObject(message);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public ObjectMessage getObjectMessage(){
		if(m_objectMessages.size() > 0 && m_objectMessages.get(0).getObject() != null)
			return m_objectMessages.remove(0);
		return null;
	}
	
	public StandardMessage getMessage(){
		if(m_standardMessages.size() > 0 && m_standardMessages.get(0).getMessage() != null){
			return m_standardMessages.remove(0);
		}
		return null;
	}
	
	private void recieveMessages(){
		while(true){
			try {
				Message message = (Message)m_inStream.readObject();
				switch (message.getType()) {
				case ObjectMessage:
					m_objectMessages.add((ObjectMessage)message);
					break;
				case StandardMessage:
					m_standardMessages.add((StandardMessage)message);
					System.out.println("Recieved: " + ((StandardMessage)message).getMessage());
				default:
					break;
				}
			} catch (ClassNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public State getState(){
		return m_state;
	}
}
