package ioio.lib.dynamixelcontrol;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class DynamixelIOIOMotor {

	
	
	InputStream is;
	OutputStream os;
	int id;
	
	int homePosition = 0xFF;
	int currentPosition = 0xFF;
	
	public DynamixelIOIOMotor(int id, InputStream input, OutputStream output) {
		
		this.id = id;
		is = input;
		os = output;
		
	}
	
	private byte[] recieveMessage() throws IOException {
		// ff ff id length error data check
		int first = is.read();
		int second = is.read();
		if (first != 0xFF || second != 0xFF) {
			Log.wtf("IOIO Dynamixel", "Incorrect start bytes on response from motor");
		}
		
		int servoID = is.read();
		if (servoID != this.id) {
			Log.d("","recieved a message from another servo " + servoID);
			//return null;
		}
		int dataLen = is.read();
		int error = is.read();
		if (error != 0) {
			Log.d("","An error was returned by the motor " + error);
		}
		
		byte[] data = new byte[dataLen - 2];
		is.read(data);
		
		for (int i = 0; i < data.length; i++) {
			Log.d("","recieved byte " + data[i]);
		}
		
		int checkSum = is.read();
		Log.d("","Recieved checksum " + checkSum);
		return data;
		
	}
	
	private byte generateChecksum(byte[] bytes) {
		
		int checksum = 0;
		for (int i = 0; i < bytes.length; i++) {
			checksum += bytes[i];
		}
		checksum = (~checksum) % 256;
		return (byte) checksum;
	}
	
	private void sendMessage(byte[] message) throws IOException {
		
		os.write(message);
		
	}
	
	private byte[] writeAddress(int id, int address, int value) {
		
		int length = 0x04;
		byte[] params = {(byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) value};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction               data             checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) value, checksum };
		try {
			sendMessage(message);
			
			recieveMessage();
			return recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	
	public void ping() {
		
		int length = 0x02;
		byte[] params = {(byte) id, (byte) length, (byte) 0x03};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction  checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x01, checksum };
		try {
			sendMessage(message);
			
			recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void setBaudRate(int newBaudRate) {
		
		
		
	}
	
	public void setID(int newID) {
		
		
		
	}
	
}
