package DCAD;

import java.awt.Color;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.LinkedList;
import java.util.concurrent.LinkedBlockingQueue;

import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class ServerConnection implements Runnable{
	private LinkedList<GObject> m_objectList;
	private LinkedBlockingQueue<Message> m_blockedMessage;
	private Socket m_socket;
	private ObjectOutputStream m_out;
	private ObjectInputStream m_in;
	private volatile boolean m_online = false;
	
	public static void main(String[] args){
		GObject object = new GObject(Shape.FILLED_OVAL, Color.RED, 23, 23, 23, 4);
		ObjectMessage message = new ObjectMessage(Message.Type.ObjectMessage, object);
		//Message message = new Message(Message.Type.StandardMessage);
		byte[] bytes = MessageConvertion.objectToBytes(message);
		System.out.println(((ObjectMessage)MessageConvertion.bytesToObject(bytes)).getObject().convertToString());
	}
	
	public ServerConnection(LinkedList<GObject> objectList){
		m_objectList = objectList;
		m_blockedMessage = new LinkedBlockingQueue<>();
		new Thread(this).start();
	}
	
	private void init() throws IOException{
		SocketAddress address = new InetSocketAddress("localhost", 25050);
		m_socket.connect(address);
		m_out = new ObjectOutputStream(m_socket.getOutputStream());
		m_in = new ObjectInputStream(m_socket.getInputStream());
	}
	
	public void updateClients(GObject current){
		ObjectMessage message = new ObjectMessage(Message.Type.ObjectMessage, current);
		checkConnected();
		if(m_online){
			try {
				m_out.writeObject(current);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				
			}
		}
	}
	
	private void checkConnected(){
		if(m_socket == null || m_in == null || m_out == null || m_socket.isClosed()){
			m_online = false;
		} else
			m_online = true;
	}

	@Override
	public void run() {
		while (true){
			Object object = null;
			switch(((Message)object).getType()){
			case ObjectMessage:
				GObject drawObject = (GObject)object;
				m_objectList.add(drawObject);
				break;
			case StandardMessage:
				StandardMessage message = (StandardMessage)object;
				System.out.println(message.getMessage());
				break;
			}
			if(m_blockedMessage.size() > 0){
				for(Message m : m_blockedMessage){
					try {
						m_out.writeObject(m);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			
		}
	}
}
