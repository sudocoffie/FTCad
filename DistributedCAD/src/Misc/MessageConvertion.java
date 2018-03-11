package Misc;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import DCAD.GObject;

public class MessageConvertion {
	public static byte[] objectToBytes(Message object){
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
		ObjectOutputStream stream = null;
		byte[] objectBytes = null;
		
		try {
			stream = new ObjectOutputStream(byteStream);
			stream.writeObject(object);
			objectBytes = byteStream.toByteArray();
			
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} finally {
			try {
				byteStream.close();
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		return objectBytes;
	}
	
	public static Object bytesToObject(byte[] bytes){
		ByteArrayInputStream byteStream = new ByteArrayInputStream(bytes);
		ObjectInputStream stream = null;
		Object inObject = null;
		
		try {
			stream = new ObjectInputStream(byteStream);
			inObject = (Object)stream.readObject();
		} catch (ClassNotFoundException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				byteStream.close();
				stream.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return inObject;
	}
}
