package ReplicaManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import Misc.StandardMessage;
import ReplicaManager.ReplicaConnection.State;

public class ReplicaManager implements Runnable {
	private ArrayList<ReplicaConnection> m_replicaConnections;
	private ArrayList<Thread> m_replicaConnectionThreads;
	private ArrayList<InetSocketAddress> m_replicaAdresses;
	public static int ID; 
	private Backup m_backup;
	private State m_state;

	public static void main(String[] args) {
		new ReplicaManager(Integer.parseInt(args[0]));
	}

	public ReplicaManager(int id) {
		ID = id;
		m_state = State.LAUNCHED;
		m_replicaConnectionThreads = new ArrayList<>();
		m_replicaConnections = new ArrayList<>();
		m_replicaAdresses = getAddressesFromConfig();
		initReplicaServerSockets();
		m_state = State.INTEGRATED;
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		startElectionProcess();
		new Thread(this).start();
	}

	@Override
	public void run() {
		listenForCrashes();
	}

	private void listenForCrashes() {
		while (true) {
			try {
				Thread.sleep(50);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			boolean primaryExists = false;
			for (ReplicaConnection c : m_replicaConnections) {
				if (c.getState() == State.PRIMARY)
					primaryExists = true;
			}
			// Starts election if primary crashes
			if (m_state != State.PRIMARY && !primaryExists && m_backup != null) {
				m_backup.setPrimaryAlive(false);
				startElectionProcess();
			}
			// Reconnects crashed replicas
			for (ReplicaConnection c : m_replicaConnections) {
				if (!c.isRunning()) {
					if (c.getId() < ID) {
						c.connectToSocket();
					} else {
						c.acceptSocketConnection();
					}
				}
			}
		}
	}

	public void startElectionProcess() {
		ReplicaConnection primary = new Election(m_replicaConnections).start(ID);
		if (primary == null) {
			// If the connection is null it's this is the primary
			m_state = State.PRIMARY;
			for (ReplicaConnection c : m_replicaConnections) {
				if (c.getState() != ReplicaConnection.State.LAUNCHED)
					c.setState(ReplicaConnection.State.BACKUP);
			}
			initFrontend();
		} else {
			// Else it's a backup
			m_state = State.BACKUP;
			for (ReplicaConnection c : m_replicaConnections) {
				if (c == primary)
					c.setState(ReplicaConnection.State.PRIMARY);
				else if (c.getState() != ReplicaConnection.State.LAUNCHED)
					c.setState(ReplicaConnection.State.BACKUP);
			}
			if (m_backup != null) {
				m_backup.setPrimary(primary);
				m_backup.setPrimaryAlive(true);
			} else
				m_backup = new Backup(primary);
		}
	}

	private ArrayList<InetSocketAddress> getAddressesFromConfig() {
		// Reads the replica addresses from replica config file
		ArrayList<InetSocketAddress> addresses = new ArrayList<>();
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src\\ReplicaManager\\ReplicaConfig"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] lineParts = line.split(" ");
				addresses.add(new InetSocketAddress(lineParts[0], Integer.parseInt(lineParts[1])));
				System.out.println("Addr:" + lineParts[0] + " Port:" + lineParts[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return addresses;
	}

	private void initReplicaServerSockets() {
		// Accepts connections from replicas with higher id's
		if (ID != m_replicaAdresses.size() - 1) {
			try {
				ServerSocket serverSocket = new ServerSocket(m_replicaAdresses.get(ID).getPort());
				for (int i = ID + 1; i < m_replicaAdresses.size(); i++) {
					ReplicaConnection r = new ReplicaConnection(serverSocket.accept(), -1);
					m_replicaConnections.add(r);
					m_replicaConnectionThreads.add(new Thread(r));
					m_replicaConnectionThreads.get(m_replicaConnectionThreads.size() - 1).start();
					StandardMessage idMessage;
					while ((idMessage = r.getMessage()) == null)
						;
					r.setId(Integer.parseInt(idMessage.getMessage()));
				}
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		// Connects to replicas with lower id's
		if (ID != 0) {
			Socket socket = null;
			for (int i = 0; i < ID; i++) {
				boolean connected = false;
				while (!connected) {
					try {
						socket = new Socket();
						socket.connect(m_replicaAdresses.get(i));
						connected = true;
					} catch (IOException e) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							e1.printStackTrace();
						}
					}
				}
				ReplicaConnection r = new ReplicaConnection(socket, i);
				m_replicaConnections.add(r);
				m_replicaConnectionThreads.add(new Thread(r));
				m_replicaConnectionThreads.get(m_replicaConnectionThreads.size() - 1).start();
				r.send(new StandardMessage(ID + ""));
			}
		}
	}

	public void initFrontend() {
		String address = "";
		int port = 0;
		// Reads from frontend config file
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
			String line;
			while ((line = reader.readLine()) != null) {
				String[] config = line.split(" ");
				address = config[0];
				port = Integer.parseInt(config[1]);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		InetSocketAddress frontendAddress = null;
		try {
			frontendAddress = new InetSocketAddress(InetAddress.getByName(address), port);
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		// Connects to frontend and creates primary
		try {
			DatagramSocket frontendSocket = new DatagramSocket();
			frontendSocket.connect(frontendAddress);
			if (m_backup != null)
				new Primary(m_replicaConnections, frontendSocket, frontendAddress, m_backup.getObjects(),
						m_backup.getClients());
			else
				new Primary(m_replicaConnections, frontendSocket, frontendAddress);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	}
}
