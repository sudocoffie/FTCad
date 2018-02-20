package Misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import DCAD.GObject;

public class MessageConvertion {
	public static byte[] objectToBytes(Message object){
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream stream;
		try {
			stream = new ObjectOutputStream(byteStream);
			stream.writeObject(object);
			return byteStream.toByteArray();
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return null;
	}
	
	public static Object bytesToObject(byte[] bytes){
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		try {
			ObjectInputStream stream = new ObjectInputStream(byteStream);
			return (Object)stream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
}
