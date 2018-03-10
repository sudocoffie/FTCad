package ReplicaManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Misc.Message;
import Misc.Message.Type;
import Misc.MessageConvertion;
import Misc.ReplyMessage;
import Misc.StandardMessage;

public class ReplicaManager {
	private ArrayList<ReplicaConnection> m_replicaConnections;
	private ArrayList<Thread> m_replicaConnectionThreads;
	ArrayList<InetSocketAddress> m_replicaAdresses;
	private int m_id;
	private Backup m_backup;

	public static void main(String[] args) {
		if(Integer.parseInt(args[1]) != 0)
			new ReplicaManager(Integer.parseInt(args[0]), true);
		else
			new ReplicaManager(Integer.parseInt(args[0]));
	}

	/**
	 * Constructor
	 * 
	 * @param id From start parameter
	 */
	public ReplicaManager(int id) {
		m_id = id;
		m_replicaConnectionThreads = new ArrayList<>();
		m_replicaConnections = new ArrayList<>();
		m_replicaAdresses = getAddressesFromConfig();
		initReplicaServerSockets();
		startElectionProcess();
	}
	
	/**
	 * Overload Constructor
	 * 
	 * @param id
	 * @param restart
	 */
	public ReplicaManager(int id, boolean restart) {
		m_id = id;
		m_replicaConnectionThreads = new ArrayList<>();
		m_replicaConnections = new ArrayList<>();
		m_replicaAdresses = getAddressesFromConfig();
		initReplicaServerSockets();
		notifyAllElection();
		startElectionProcess();
	}
	
	public void notifyAllElection() {
		for(ReplicaConnection c : m_replicaConnections) {
			c.send(new StandardMessage("ELECTIONMESSAGE"));
		}
	}
	
	/**
	 * Execute the election process for choosing the primary server
	 * 
	 * @param none
	 * @return none
	 */
	public void startElectionProcess() {
		ReplicaConnection primary = new Election(m_replicaConnections).start(m_id);

		if (primary == null) {
			for (ReplicaConnection c : m_replicaConnections) {
				if (c.getState() != ReplicaConnection.State.LAUNCHED)
					c.setState(ReplicaConnection.State.BACKUP);
			}
			initFrontend();
		} else {
			for (ReplicaConnection c : m_replicaConnections) {
				if (c == primary)
					c.setState(ReplicaConnection.State.PRIMARY);
				else
					c.setState(ReplicaConnection.State.BACKUP);
			}
			m_backup = new Backup(primary);
		}
	}

	/**
	 * Gets the addresses from the config and sets them in m_replicaAddresses
	 * 
	 * @param none
	 * @return ArrayList<InetSocketAddress> ServerAddresses
	 */
	private ArrayList<InetSocketAddress> getAddressesFromConfig() {
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

	/**
	 * Sets up connection and start ReplicaConnections
	 * 
	 * 
	 * @param none
	 */
	private void initReplicaServerSockets() {
		System.out.println(m_id + ": 1");
		if (m_id != m_replicaAdresses.size() - 1) {
			System.out.println(m_id + ": 2");
			try {
				ServerSocket serverSocket = new ServerSocket(m_replicaAdresses.get(m_id).getPort());
				for (int i = m_id + 1; i < m_replicaAdresses.size(); i++) {
					ReplicaConnection r = new ReplicaConnection(serverSocket.accept(), this);
					m_replicaConnections.add(r);
					m_replicaConnectionThreads.add(new Thread(r));
					m_replicaConnectionThreads.get(m_replicaConnectionThreads.size() - 1).start();
					System.out.println(m_id + ": 3");
				}
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (m_id != 0) {
			Socket socket = null;
			System.out.println(m_id + ": 4");
			for (int i = 0; i < m_id; i++) {
				boolean connected = false;
				while (!connected) {
					try {
						System.out.println(m_id + ": 5");
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
				
				ReplicaConnection r = new ReplicaConnection(socket, this);
				m_replicaConnections.add(r);
				m_replicaConnectionThreads.add(new Thread(r));
				m_replicaConnectionThreads.get(m_replicaConnectionThreads.size() - 1).start();
			}
		}
		System.out.println(m_id + ": 6");
		try {
			Thread.sleep(100 * m_id);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Initilize frontend
	 * 
	 * @param none
	 * @return none
	 */
	public void initFrontend() {
		String address = "";
		int port = 0;

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
		try {
			DatagramSocket frontendSocket = new DatagramSocket();
			frontendSocket.connect(frontendAddress);
			new Primary(m_replicaConnections, frontendSocket, frontendAddress);
		} catch (SocketException e1) {
			e1.printStackTrace();
		}
	}
}
