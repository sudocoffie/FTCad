package Frontend;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import Misc.Message;
import Misc.MessageConvertion;
import Misc.StandardMessage;

public class Frontend {
	private int m_serverPort = 0;
	private int m_clientPort = 0;
	private InetAddress m_address;
	private int m_port;
	private DatagramSocket m_clientSocket, m_serverSocket;

	public Frontend() {
		// Reads from frontend config
		try {
			BufferedReader frontendConfig = new BufferedReader(new FileReader("src\\Frontend\\FrontendConfig"));
			String line;

			while ((line = frontendConfig.readLine()) != null) {
				m_serverPort = Integer.parseInt(line.split(" ")[1]);
				m_clientPort = Integer.parseInt(line.split(" ")[2]);
			}
			frontendConfig.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		new Thread(new Runnable() {
			public void run() {
				try {
					m_serverSocket = new DatagramSocket(m_serverPort);
					ServerListener();
				} catch (SocketException e) {
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
					e.printStackTrace();
				}
			}
		}).start();

	}

	public static void main(String[] args) {
		new Frontend();
	}

	private void ServerListener() {
		while (true) {
			DatagramPacket packet = recieveMessage(m_serverSocket);
			// Saves the last address/port of the primary
			if (packet.getPort() != m_port)
				m_port = packet.getPort();
			if (!packet.getAddress().equals(m_address))
				m_address = packet.getAddress();
			Message message = (Message) MessageConvertion.bytesToObject(packet.getData());
			if (message.getType() == Message.Type.STANDARDMESSAGE)
				System.out.println(((StandardMessage) message).getMessage() + " to ");
			// Sends the message to the specified client
			if (message.getAddress() != null && message.getPort() != -1)
				sendMessage(packet.getData(), m_clientSocket, message.getAddress(), message.getPort());
		}
	}

	private void ClientListener() {
		while (true) {
			// Receives messages from all clients and forwards them to the primary
			DatagramPacket packet = recieveMessage(m_clientSocket);
			Message message = (Message) MessageConvertion.bytesToObject(packet.getData());
			System.out.println("C Type: " + message.getType());
			if (message.getType() == Message.Type.STANDARDMESSAGE)
				System.out.println(((StandardMessage) message).getMessage());
			sendMessage(packet.getData(), m_serverSocket, m_address, m_port);
		}
	}

	public void sendMessage(byte[] message, DatagramSocket socket, InetAddress address, int port) {
		byte[] buf = message;
		DatagramPacket marshing_packet;
		try {
			marshing_packet = new DatagramPacket(buf, buf.length, address, port);
			socket.send(marshing_packet);
		} catch (NullPointerException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			System.err.println("Server offline! (Frontend.sendMessage())");
		}
	}

	public DatagramPacket recieveMessage(DatagramSocket socket) {
		byte[] buf = new byte[8192];
		DatagramPacket marshing_packet = new DatagramPacket(buf, buf.length);
		try {
			socket.receive(marshing_packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return marshing_packet;
	}
}
