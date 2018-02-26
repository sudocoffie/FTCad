package ReplicaManager;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.ArrayList;

public class ReplicaManager {
	private ArrayList<ServerConnection> m_replicaConnections;
	ArrayList<InetSocketAddress> m_replicaAdresses;
	private int m_id;
	
	public static void main(String[] args){
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
			while((line = reader.readLine()) != null){
				address = line.split(" ")[0];
				port = Integer.parseInt(line.split(" ")[1]);
				m_replicaAdresses.add(new InetSocketAddress(address, port));
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if(id != m_replicaAdresses.size() - 1){
			try {
				ServerSocket serverSocket = new ServerSocket(m_replicaAdresses.get(id).getPort());
				for(int i = id + 1; i < m_replicaAdresses.size(); i++){
					m_replicaConnections.add(new ServerConnection(serverSocket.accept()));
				}
				serverSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if(id != 0){
			Socket socket = null;
			for(int i = 0; i < id; i++){
				boolean connected = false;
				while(!connected){
					try {
						socket = new Socket();
						socket.connect(m_replicaAdresses.get(i));
						connected = true;
					} catch (IOException e) {
						// TODO Auto-generated catch block
						//e.printStackTrace();
						try {
							Thread.sleep(100);
						} catch (InterruptedException e1) {
							// TODO Auto-generated catch block
							e1.printStackTrace();
						}
					}
				}
				m_replicaConnections.add(new ServerConnection(socket));
			}
		}
	}
	
	private void initFrontend(){
		String address = null;
		int port = 0;
		try {
			BufferedReader reader = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
			String line;
			while((line = reader.readLine()) != null){
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
			DatagramSocket socket = new DatagramSocket();
			socket.connect(frontendAddress);
			listenFrontend(socket);
		} catch (SocketException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	private void listenFrontend(DatagramSocket socket){
		
	}
}
