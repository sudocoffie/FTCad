package ReplicaManager;

import java.util.ArrayList;
import Misc.StandardMessage;

public class Election {
	private boolean m_startedcampaign;
	private ArrayList<ReplicaConnection> m_connections;

	public Election(ArrayList<ReplicaConnection> connections) {
		m_connections = new ArrayList<>();
		m_startedcampaign = false;
		// Filter out the replicas that haven't been integrated
		for (ReplicaConnection c : connections) {
			if (c.getState() != ReplicaConnection.State.LAUNCHED)
				m_connections.add(c);
		}
	}

	public ReplicaConnection start(int id) {
		// This algorithm follows the bully protocol
		// If not it starts an election
		StandardMessage message;
		boolean electionStarted = false;
		int yesCounter = 0;
		while (true) {
			try {
				Thread.sleep(10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			// Checks if any election is present and votes if thats the case
			for (ReplicaConnection c : m_connections) {
				if ((message = c.getMessage()) != null && message.getMessage().startsWith("election")) {
					electionStarted = true;
					String[] electionMessage = message.getMessage().split(" ");
					switch (electionMessage[1]) {
					case "yes":
						yesCounter++;
						// If everyone votes yes the election is won
						if (yesCounter == m_connections.size()) {
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
						// Votes yes or no depending on if their id is higher or lower
						if (Integer.parseInt(electionMessage[1]) > id) {
							c.send(new StandardMessage("election yes"));
						} else {
							c.send(new StandardMessage("election no"));
							startCampaign(id);
						}
						break;
					}
				}
			}
			// Starts election if no election is present
			if (!electionStarted) {
				startCampaign(id);
				electionStarted = true;
			}
		}
	}

	private void wonElection(int id) {
		for (ReplicaConnection c : m_connections)
			c.send(new StandardMessage("election won " + id));
		System.out.println("I won");
	}

	private void startCampaign(int id) {
		if (!m_startedcampaign) {
			for (ReplicaConnection c : m_connections)
				c.send(new StandardMessage("election " + id));
			m_startedcampaign = true;
		}
	}
}
