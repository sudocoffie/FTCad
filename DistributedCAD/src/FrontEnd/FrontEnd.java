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

	public FrontEnd() {

		new Thread(new Runnable() {
			public void run() {

				try {
					DatagramSocket m_serverSocket = new DatagramSocket(20049);
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
	
	public static void main(String[] args){
		new FrontEnd();
	}

	// kapa tr�d for socket
	// g�ra tv� metoder f�r send recieve fr�n client tr�d och server
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
			if (add){
				savePort.add(message.getPort());
				System.out.println("Client connected port: " + message.getPort());
			}
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
