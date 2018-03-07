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

import Misc.Message;
import Misc.Message.Type;
import Misc.MessageConvertion;
import Misc.ReplyMessage;

public class Frontend {
	private ArrayList<Integer> savePort = new ArrayList<>();
	private int m_serverPort = 0;
	private int m_clientPort = 0;
	private InetAddress m_address;
	private int m_port;
	private DatagramSocket m_clientSocket, m_serverSocket;
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
					m_serverSocket = new DatagramSocket(m_serverPort);
					ServerListener();
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					m_clientSocket = new DatagramSocket(m_clientPort);
					ClientListener();
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
	private void ServerListener() {
		DatagramPacket packet = recieveMessage(m_serverSocket);
		// send to clients
		if(packet.getPort() != m_port)
			m_port = packet.getPort();
		if(!packet.getAddress().equals(m_address))
			m_address = packet.getAddress();
		Message message = (Message)MessageConvertion.bytesToObject(packet.getData());
		sendMessage(packet.getData(), m_clientSocket, message.getAddress(), message.getPort());

	}

	private void ClientListener() {
		while (true) {
			DatagramPacket packet = recieveMessage(m_clientSocket);
			// Reply to a message, will be similar in replica
			//Message message = (Message)MessageConvertion.bytesToObject(packet.getData());
			//System.out.println("Type: " + message.getType());
			//ReplyMessage reply = new ReplyMessage(Type.ReplyMessage, packet.getAddress(), packet.getPort(), message.getId());
			//sendMessage(MessageConvertion.objectToBytes(reply), m_clientSocket, packet.getAddress(), packet.getPort());
			sendMessage(packet.getData(), m_serverSocket, m_address, m_port);
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
			System.err.println("Server offline! (Frontend.sendMessage())");
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
