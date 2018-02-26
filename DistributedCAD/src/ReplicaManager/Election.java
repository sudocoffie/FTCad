package ReplicaManager;

import java.util.ArrayList;

public class Election {
	private ArrayList<ServerConnection> m_connections;
	public Election(ArrayList<ServerConnection> connections){
		m_connections = new ArrayList<>();
		for(ServerConnection c : connections){
			if(c.getState() != ServerConnection.State.LAUNCHED)
				m_connections.add(c);
		}
	}
}
