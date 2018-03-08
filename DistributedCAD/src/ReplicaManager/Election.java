package ReplicaManager;

import java.util.ArrayList;

import DCAD.ServerConnection;
import Misc.Message;
import Misc.Message.Type;
import Misc.StandardMessage;

public class Election {
	private boolean m_startedcampaign;
	private ArrayList<ReplicaConnection> m_connections;
	public Election(ArrayList<ReplicaConnection> connections){
		m_connections = new ArrayList<>();
		m_startedcampaign = false;
		for(ReplicaConnection c : connections){
			if(c.getState() != ReplicaConnection.State.LAUNCHED)
				m_connections.add(c);
		}
	}
	
	public ReplicaConnection start(int id){
		StandardMessage message;
		boolean electionStarted = false;
		int yesCounter = 0;
		while(true){
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			for(ReplicaConnection c : m_connections){
				if((message = c.getMessage()) != null && message.getMessage().startsWith("election")){
					electionStarted = true;
					String[] electionMessage = message.getMessage().split(" ");
					switch (electionMessage[1]) {
						case "yes":
							yesCounter++;
							System.out.println(yesCounter + " " + m_connections.size());
							if(yesCounter == m_connections.size()){
								wonElection(id);
								return null;
							}
							break;
						case "no":
							yesCounter = 0;
							break;
						case "won":
							System.out.println(Integer.parseInt(electionMessage[2]) + " won");
							return c;
						default:
							if(Integer.parseInt(electionMessage[1]) > id){
								c.send(new StandardMessage("election yes"));
							} else{
								c.send(new StandardMessage("election no"));
								startCampaign(id);
							}
							break;
					}
				}
			}
			if(!electionStarted){
				startCampaign(id);
				electionStarted = true;
			}
		}
	}
	
	private void wonElection(int id){
		for(ReplicaConnection c : m_connections)
			c.send(new StandardMessage("election won " + id));
		System.out.println("I won");
	}
	
	private void startCampaign(int id){
		if(!m_startedcampaign){
			for(ReplicaConnection c : m_connections)
				c.send(new StandardMessage("election " + id));
			m_startedcampaign = true;
		}
	}
}
