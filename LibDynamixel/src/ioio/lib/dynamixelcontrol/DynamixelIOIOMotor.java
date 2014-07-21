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
			if (second == 0xFF) {
				first = is.read();
				if (first == 0xFF) {
					Log.e("","Fixed");
				}
			}
		}
		
		int servoID = is.read();
		if (servoID != this.id) {
			Log.e("","recieved a message from another servo " + servoID);
			//return null;
		}
		int dataLen = is.read();
		int error = is.read();
		if (error != 0) {
			Log.e("","An error was returned by the motor " + error);
		}
		
		byte[] data = new byte[dataLen - 2];
		is.read(data);
		int checkSum = is.read();
		Log.e("","Recieved checksum " + checkSum);
		return data;
		
	}
	
	private byte generateChecksum(byte[] message, byte id) {
		
		int checksum = message.length;
		for (int i = 0; i < message.length; i++) {
			checksum += message[i];
		}
		checksum = (~checksum) % 256;
		return (byte) checksum;
	}
	
	private void sendMessage(int id, byte[] message) throws IOException {
		
		byte[] buffer = new byte[2 + 1 + 1 + message.length + 1];//start bits + id + length + message + checksum
		// ff ff id length instruction data checksum
		buffer[0] = (byte) 0xFF;
		buffer[1] = (byte) 0xFF;
		buffer[2] = (byte) id;
		buffer[3] = (byte) (message.length + 1);
		for (int i = 0; i < message.length; i++) {
			buffer[i + 4] = message[i];
		}
		buffer[buffer.length - 1] = generateChecksum(message, (byte)id);	
		
		os.write(buffer);
		
	}
	
	/*private byte[] readAddress(int id, byte[] values) {
		
		try {
			//recieveMessage();
			return recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}*/
	
	private byte[] writeAddress(int id, byte[] values) {
		
		try {
			sendMessage(id, values);
			recieveMessage();
			return recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
		
	}
	

	
	public void changeID(int id) { //ONLY TO BE USED IN SET UP
		//Changes the ID of any connected motor to id
		//Only connect one motor at a time when using this method
		byte[] values = new byte[3];
		
		values[0] = 0x03;//Write command
		values[1] = 0x03;//Address
		values[2] = (byte) (id & 0xFF);
		for (int i = 0; i < 253; i++){
			//writeAddress(i, values);
			resetMotor(id);
		}
		writeAddress(1,values);
		ping(id);
		
	}
	

	
	public void setLEDColor(byte color) {
		
		byte[] values = new byte[3];
		
		values[0] = 0x03;//Write command
		values[1] = 0x19;//Address
		values[2] = color;//Value to set
		writeAddress(this.id, values);
		
	}

	public int readCurrentVoltage() {
		
		//byte address = 0x19;
		byte[] values = new byte[3];
		
		values[0] = 0x02;//Read command
		values[1] = 0x2A;//Address
		values[2] = 0x01;//Number of bytes to read
		
		return writeAddress(this.id, values)[0] / 10;
		
	}
	
	public void resetMotor(int id) {
		
		byte[] values = new byte[1];
		
		values[0] = 0x06;//Reset command
		writeAddress(id, values);
		
	}
	public void setBaudRate(byte rate) {
		
		byte[] values = new byte[3];
		
		values[0] = 0x03;//Write command
		values[1] = 0x04;//Address
		values[2] = rate;//Value to set
		writeAddress(this.id, values);
		
	}
	
	public void ping(int id) {
		
		byte[] values = new byte[1];
		
		values[0] = 0x01;//Ping command
		writeAddress(id, values);
		
	}
	
}
