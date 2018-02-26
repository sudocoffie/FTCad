package ReplicaManager;

import java.net.Socket;

public class ServerConnection {
	public enum State{LAUNCHED, INTEGRATED, PRIMARY, BACKUP}
	private State m_state;
	public ServerConnection(Socket socket){
		m_state = State.LAUNCHED;
		System.out.println("Connected to: " + socket.getPort());
	}
	
	public State getState(){
		return m_state;
	}
}
