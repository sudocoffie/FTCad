package Frontend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Frontend {
	private ArrayList<Integer> savePort = new ArrayList<>();
	private int m_serverPort = 0;
	private int m_clientPort = 0;
	private InetAddress m_address;
	private int m_port;
	public Frontend() {
		try {
			BufferedReader frontendConfig = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
			String line;

			while ((line = frontendConfig.readLine()) != null) {
				m_serverPort = Integer.parseInt(line.split(" ")[1]);
				m_clientPort = Integer.parseInt(line.split(" ")[2]);
			}
			frontendConfig.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			public void run() {

				try {

					DatagramSocket m_serverSocket = new DatagramSocket(m_serverPort);
					ServerListener(m_serverSocket);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					DatagramSocket m_clientSocket = new DatagramSocket(m_clientPort);
					ClientListener(m_clientSocket);
					
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		
	}

	public static void main(String[] args) {
		new Frontend();
	}

	// kapa tr�d for socket
	// g�ra tv� metoder f�r send recieve fr�n client tr�d och server
	private void ServerListener(DatagramSocket socket) {
		DatagramPacket message = recieveMessage(socket);
		// send to clients
		if(message.getPort() != m_port)
			m_port = message.getPort();
		if(!message.getAddress().equals(m_address))
			m_address = message.getAddress();
		sendMessage(message.getData(), socket, m_address, m_port);

	}

	private void ClientListener(DatagramSocket socket) {
		while (true) {
			DatagramPacket message = recieveMessage(socket);
			boolean add = true;
			for (Integer i : savePort) {
				if (message.getPort() == i)
					add = false;
			}
			if (add) {
				savePort.add(message.getPort());
				System.out.println("Client connected port: " + message.getPort());
			}
			for (Integer i : savePort)
				sendMessage(message.getData(), socket,m_address, i);
		}

	}

	public void sendMessage(byte[] message, DatagramSocket socket, InetAddress address, int port) {
		byte[] buf = message;
		DatagramPacket marshing_packet;
		
		try {
		
			marshing_packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(marshing_packet);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public DatagramPacket recieveMessage(DatagramSocket socket) {
		// ta emot meddelande fr�n en client

		byte[] buf = new byte[1024];
		DatagramPacket marshing_packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(marshing_packet);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return marshing_packet;

	}
}
