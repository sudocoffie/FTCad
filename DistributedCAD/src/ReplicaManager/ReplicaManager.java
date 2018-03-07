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

public class ReplicaManager {
	private ArrayList<ReplicaConnection> m_replicaConnections;
	ArrayList<InetSocketAddress> m_replicaAdresses;
	private int m_id;
	
	public static void main(String[] args) {
		new ReplicaManager(Integer.parseInt(args[0]));
	}

	public ReplicaManager(int id) {
		m_id = id;
		System.out.println(id);
		m_replicaConnections = new ArrayList<>();
		m_replicaAdresses = new ArrayList<>();
		String address = "";
		int port = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src\\ReplicaManager\\ReplicaConfig"));
			String line;
			while ((line = reader.readLine()) != null) {
				address = line.split(" ")[0];
				port = Integer.parseInt(line.split(" ")[1]);
				System.out.println(address + " " + port);
				m_replicaAdresses.add(new InetSocketAddress(address, port));
				System.out.println(id + ": 0");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println(id + ": 1");
		if (id != m_replicaAdresses.size() - 1) {
			System.out.println(id + ": 2");
			try {
				System.out.println(m_replicaAdresses.toString());
				ServerSocket serverSocket = new ServerSocket(m_replicaAdresses.get(id).getPort());
				for (int i = id + 1; i < m_replicaAdresses.size(); i++) {
					m_replicaConnections.add(new ReplicaConnection(serverSocket.accept()));
					System.out.println(id + ": 3");
				}
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (id != 0) {
			Socket socket = null;
			System.out.println(id + ": 4");
			for (int i = 0; i < id; i++) {
				boolean connected = false;
				while (!connected) {
					try {
						System.out.println(id + ": 5");
						socket = new Socket();
						socket.connect(m_replicaAdresses.get(i));
						connected = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						// e.printStackTrace();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				m_replicaConnections.add(new ReplicaConnection(socket));
			}
		}
		System.out.println(id + ": 6");
		try {
			Thread.sleep(100 * id);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		int primary = new Election(m_replicaConnections).start(m_id);
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		SocketAddress frontendAddress = new InetSocketAddress(address, port);
		try {
			DatagramSocket frontendSocket = new DatagramSocket();
			frontendSocket.connect(frontendAddress);
			new Primary(m_replicaConnections,frontendSocket);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}

	public void sendMessage(byte[] message, DatagramSocket socket, String address, int port) {
		byte[] buf = message;
		DatagramPacket m_packet;
		try {
			m_packet = new DatagramPacket(buf, buf.length, port);
			socket.send(m_packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DatagramPacket recieveMessage(DatagramSocket socket) {
		byte[] buf = new byte[1024];
		DatagramPacket m_packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(m_packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return m_packet;

	}
}
