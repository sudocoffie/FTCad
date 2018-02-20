package FrontEnd;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class FrontEnd {
	private ArrayList<Integer> savePort = new ArrayList<>();

	public FrontEnd(int port) {

		new Thread(new Runnable() {
			public void run() {

				try {
					DatagramSocket m_serverSocket = new DatagramSocket(20050);
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
					DatagramSocket m_clientSocket = new DatagramSocket(20050);
					ClientListener(m_clientSocket);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}

			}
		}).start();
	}

	// kapa tråd for socket
	// göra två metoder för send recieve från client tråd och server
	private void ServerListener(DatagramSocket socket) {

	}

	private void ClientListener(DatagramSocket socket) {
		while (true) {
			DatagramPacket message = recieveMessage(socket);
			boolean add = true;
			for (Integer i : savePort) {
				if (message.getPort() == i)
					add = false;
			}
			if (add)
				savePort.add(message.getPort());

			for (Integer i : savePort)
				sendMessage(message.getData(), socket, i);
		}

	}

	public void sendMessage(byte[] message, DatagramSocket socket, int port) {
		byte[] buf = message;
		DatagramPacket marshing_packet;
		try {
			marshing_packet = new DatagramPacket(buf, buf.length, InetAddress.getByName("localhost"), port);
			socket.send(marshing_packet);
		} catch (UnknownHostException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		// skicka meedeland e til alla clienter
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public DatagramPacket recieveMessage(DatagramSocket socket) {
		// ta emot meddelande från en client

		byte[] buf = new byte[256];
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
