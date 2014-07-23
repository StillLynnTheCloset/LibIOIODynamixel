package ioio.lib.dynamixelcontrol;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class DynamixelIOIOMotor {
	
	InputStream is;
	OutputStream os;
	DigitalOutput comLock;
	int id;
	
	int homePosition = 0xFF;
	int currentPosition = 0xFF;
	
	public DynamixelIOIOMotor(int id, InputStream input, OutputStream output, DigitalOutput comLock) {
		
		this.id = id;
		is = input;
		os = output;
		this.comLock = comLock;
		
	}
	
	private byte[] recieveMessage() throws IOException, ConnectionLostException {
		// ff ff id length error data check
		comLock.write(false);
		//int timeOut = 100;
		int first = is.read();
		int second = is.read();
		/*while (timeOut < 100 && (first != 0xFF || second != 0xFF)) {
			
			if (timeOut % 2 == 0) {
				first = is.read();
			} else {
				second = is.read();
			}
			
		}*/
		
		int servoID = is.read();
		if (servoID != this.id) {
			Log.e("Receiving","recieved a message from another servo " + servoID);
			//return null;
		}
		int dataLen = is.read();
		int error = is.read();
		if (error != 0) {
			Log.e("Receiving","An error was returned by the motor " + error);
		}
		
		byte[] data = new byte[dataLen - 2];
		is.read(data);
		
		for (int i = 0; i < data.length; i++) {
			Log.d("Receiving","recieved byte " + data[i]);
		}
		
		int checkSum = is.read();
		Log.d("Receiving","Recieved checksum " + checkSum);
		
		comLock.write(true);
		return data;
		
	}
	
	private byte generateChecksum(byte[] bytes) {
		
		int checksum = 0;
		for (int i = 0; i < bytes.length; i++) {
			checksum += bytes[i];
		}
		checksum = (~checksum);
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
			comLock.write(true);
			
			sendMessage(message);
		} catch (IOException | ConnectionLostException e) {
			e.printStackTrace();
			
		}
		try {
			//Thread.sleep(20);
			comLock.write(false);
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} //catch (InterruptedException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		//}
		Log.d("","Finished writing address");
		return null;
		
		
	}
	
	public void ping() {
		
		int length = 0x02;
		byte[] params = {(byte) id, (byte) length, (byte) 0x01};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction  checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x01, checksum };
		try {
			sendMessage(message);
			is.skip(message.length);
			recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void setBaudRate(int newBaudRate) {
		
		writeAddress(id, 4, convertBaudRate(newBaudRate));
		
	}
	
	private byte convertBaudRate(int rate) {
		
		switch (rate) {
		case 9600:
			return (byte) 207;
		case 19200:
			return 103;
		case 38400:
			return 51;
		case 57600:
			return 34;
		case 115200:
			return 16;
		case 200000:
			return 9;
		case 250000:
			return 7;
		case 400000:
			return 4;
		case 500000:
			return 3;
		case 1000000:
			return 1;
		default:
			return 1;
		}
		
	}
	
	public void setID(int newID) {
		
		writeAddress(id, 3, newID);
		this.id = newID;
		
	}
	
	public void setLEDColor(int color) {
		
		writeAddress(id, 25, color);
		
	}
	
	public void reset() {
		
		int length = 0x02;
		byte[] params = {(byte) id, (byte) length, (byte) 0x06};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction  checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x06, checksum };
		try {
			sendMessage(message);

		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void moveToPosition(int position) {
		
		if (position > 1023) {
			Log.e("","Tried to move to invalid position");
			return;
		}
		if (position < 0) {
			Log.e("","Tried to move to invalid position");
			return;
		}
		Log.d("","Move to position " + position + "    " + (position & 0xFF) + "   " + ((position & 0xFF00) >> 8));
		writeAddress(id, 30, position % 256);
		try {
			recieveMessage();
		} catch (IOException | ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		writeAddress(id, 31, position / 256);
		
		//Log.e("","Moved");
		
	}
	
	public void setSpeed(int speed) {
		
		if (speed > 1023) {
			Log.e("","Tried to set speed to invalid speed");
			return;
		}
		if (speed < 0) {
			Log.e("","Tried to move to invalid position");
			return;
		}
		Log.e("","Move to speed " + speed + "    " + (speed & 0xFF) + "   " + ((speed & 0xFF00) >> 8));
		writeAddress(id, 32, speed % 256);
		
		writeAddress(id, 33, speed / 256);
		//Log.e("","Moved");
		
	}
	
	public void setTorqueEnable(int enable) {
		
		writeAddress(id, 24, enable & 1);
		
	}
	
	public void setMinAngle(int angle) {
		
		writeAddress(id, 6, angle % 256);
		
		writeAddress(id, 7, angle / 256);
		
	}
	
	public void setMaxAngle(int angle) {
		
		writeAddress(id, 8, angle % 256);
		
		writeAddress(id, 9, angle / 256);
		
	}
	
}
