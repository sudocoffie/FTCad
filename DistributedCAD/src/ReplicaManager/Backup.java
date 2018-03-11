package ReplicaManager;

import java.util.ArrayList;

import DCAD.GObject;
import Misc.ObjectListMessage;
import Misc.ObjectMessage;
import Misc.StandardMessage;

public class Backup implements Runnable {
	
	private ReplicaConnection m_primary;
	private boolean m_primaryAlive;
	private ArrayList<GObject> m_objects;
	private ArrayList<String> m_clients;
	public Backup(ReplicaConnection primary){
		m_primary = primary;
		m_primaryAlive = true;
		m_objects = new ArrayList<>();
		m_clients = new ArrayList<>();
		new Thread(this).start();
	}
	

	@Override
	public void run() {
		while(m_primaryAlive) {
			StandardMessage sMessage;
			while((sMessage = m_primary.getMessage()) != null) {
				if(sMessage.getMessage().startsWith("removeClient")){
					m_clients.remove(sMessage.getMessage().substring(sMessage.getMessage().indexOf(" ") + 1));
				}
				else if(sMessage.getMessage().startsWith("remove")) {
					GObject remove = null;
					for(GObject o : m_objects) {
						if(o.getId().toString().equals(sMessage.getMessage().split(" ")[1]))
							remove = o;
					}
					m_objects.remove(remove);
				}
				else if(!sMessage.getMessage().startsWith("election")) {
					m_clients.add(sMessage.getMessage());
				}
			}
			ObjectMessage oMessage;
			while((oMessage = m_primary.getObjectMessage()) != null) {
				m_objects.add(oMessage.getObject());
			}
			ObjectListMessage oLMessage;
			while((oLMessage = m_primary.getObjectListMessage()) != null) {
				GObject[] objects = oLMessage.getObjects();
				for(int i = 0; i < objects.length; i++)
					m_objects.add(objects[i]);
			}
		}
	}
	
	public ArrayList<GObject> getObjects(){
		return m_objects;
	}
	
	public ArrayList<String> getClients(){
		return m_clients;
	}
	
	public void setPrimaryAlive(boolean value) {
		m_primaryAlive = value;
	}
}
