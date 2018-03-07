package ReplicaManager;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.UUID;

import DCAD.GObject;
import Misc.Message;
import Misc.MessageConvertion;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class Primary implements Runnable {
	private ArrayList<ReplicaConnection> m_connection = new ArrayList<>();
	private DatagramSocket m_socket;
	private ArrayList<GObject> m_object = new ArrayList<>();
	public Primary(ArrayList<ReplicaConnection> connections, DatagramSocket socket) {
		m_connection = connections;
		m_socket = socket;
		new Thread(this).start();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		byte[] buf = new byte[1024];
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		try {
			m_socket.receive(packet);
			Message message = (Message) MessageConvertion.bytesToObject(packet.getData());
			switch (message.getType()) {
			case StandardMessage:
				StandardMessage standardMessage = (StandardMessage) message;
				if(standardMessage.getMessage().startsWith("join")) {
					System.out.println("you connected" + standardMessage.getMessage());
				}
				else if(standardMessage.getMessage().startsWith("remove")) {
					System.out.println("you removed " + standardMessage.getMessage());
				}
				//join ett meddelande "join"
				//remove med ett UUID
				break;
			case ObjectMessage:
				boolean addObject = true;
				ObjectMessage objMessage = (ObjectMessage) message;
				for(GObject object : m_object) {
					if(object.getId().equals())
				}
				
				if(onjMessage.getObject()) {
					
				}
				
				break;
			case ReplyMessage:
				break;
			default:
				break;
			}
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

	private void sendToAllBackups(Socket socket) {
		// send backups
		ReplicaConnection instance = new ReplicaConnection(socket);

		for (ReplicaConnection searchForBackup : m_connection) {
			if (searchForBackup.getState() == ReplicaConnection.State.BACKUP) {
				Message message = (Message) MessageConvertion.bytesToObject();
				instance.send(message);
			}
		}
	}

	public void remove() {
		if(message.getMessage().startsWith("remove")) {
            GObject remove = null;
            for(GObject o : m_objectList) {
                if(o.getId().equals(message.getMessage().split(" ")[1]))
                    remove = o;
            }
            m_objectList.remove(remove);
        }
	}

}
