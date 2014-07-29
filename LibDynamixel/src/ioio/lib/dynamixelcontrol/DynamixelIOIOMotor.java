/*
 * The MIT License (MIT)
 *
 *Copyright (c) 2014 Eric Hochendoner
 *
 *Permission is hereby granted, free of charge, to any person obtaining a copy
 *of this software and associated documentation files (the "Software"), to deal
 *in the Software without restriction, including without limitation the rights
 *to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *copies of the Software, and to permit persons to whom the Software is
 *furnished to do so, subject to the following conditions:
 *
 *The above copyright notice and this permission notice shall be included in all
 *copies or substantial portions of the Software.
 *
 *THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *SOFTWARE. 
 */
package ioio.lib.dynamixelcontrol;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.exception.ConnectionLostException;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;

public class DynamixelIOIOMotor {
	
	public final static int MOTOR_AX12A = 0;
	public final static int MOTOR_MX28T = 1;
	
	private InputStream is;
	private OutputStream os;
	private DigitalOutput comLock;
	private int id;
	private int motorType;
	private int minAngleLimit = 0;
	private int maxAngleLimit = 300;
	private double encoderPerSecPerUnit = 1.58;
	//private int homePosition = 0xFF;
	//private int currentPosition = 0xFF;
	
	public DynamixelIOIOMotor(int id, InputStream input, OutputStream output, DigitalOutput comLock) {
		
		this.id = id;
		is = input;
		os = output;
		this.comLock = comLock;
		
	}
	
	public void setMotorType(int motor) {
		

		
	}
	
	private byte[] recieveMessage() throws IOException, ConnectionLostException {
		// ff ff id length error data check
		comLock.write(false);
//		int timeOut = 100;
		int first = is.read();
		int second = is.read();
		
		if (first != 0xFF || second != 0xFF) {
			
			Log.e("Receiving","received bad start bytes");
//			while (timeOut < 100 && (first != 0xFF || second != 0xFF)) {
//				
//				if (timeOut % 2 == 0) {
//					first = is.read();
//				} else {
//					second = is.read();
//				}
//				
//			}
		}
		
		
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
			Thread.sleep(30);
			sendMessage(message);
			//recieveMessage();
			//comLock.write(false);
		} catch (IOException | ConnectionLostException e) {
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Log.d("","Finished writing address");
		return null;
		
		
	}
	
	private byte[] writeAddress(int id, int address, int lowByte, int highByte) {
		
		int length = 0x05;
		byte[] params = {(byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) lowByte, (byte) highByte};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction                                  data             checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) lowByte, (byte) highByte, checksum };
		try {
			comLock.write(true);
			Thread.sleep(30);
			sendMessage(message);
			comLock.write(false);
		} catch (IOException | ConnectionLostException e) {
			e.printStackTrace();
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
			//is.skip(message.length);
			recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConnectionLostException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		
	}
	
	public void setBaudRate(int newBaudRate) {
		
		writeAddress(id, 0x04, convertBaudRate(newBaudRate));
		
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
		
		writeAddress(id, 0x03, newID);
		this.id = newID;
		
	}
	
	public void setLEDColor(int color) {
		
		writeAddress(id, 0x19, color);
		
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
		Log.e("","Move to position " + position + "    " + position % 256 + "   " + position / 256);
		writeAddress(id, 0x1E, position % 256, position / 256);
		
	}
	
	public void setSpeed(int speed) {
		
		if (speed > 2047) {
			Log.e("","Tried to set speed to invalid speed");
			return;
		}
		if (speed < 0) {
			Log.e("","Tried to move to invalid speed");
			return;
		}
		Log.e("","Move at speed " + speed + "    " + speed % 256 + "   " + speed / 256);
		writeAddress(id, 0x20, speed % 256, speed / 256);

		
	}
	
	public void setTorqueEnable(int enable) {
		
		writeAddress(id, 0x18, enable);
		
	}
	
	public void setMinAngle(int angle) {
		
		writeAddress(id, 0x06, angle % 256, angle / 256);

	}
	
	public void setMaxAngle(int angle) {
		
		writeAddress(id, 0x08, angle % 256, angle / 256);
		
	}
	
	public void setContinousRotation(int enable) {
		
		if (enable == 0) {
			setMinAngle(1);
			setMaxAngle(1023);
		} else {
			setMinAngle(0);
			setMaxAngle(0);
		}
		
	}
	
	public int getSpeedFromCoefficient(double coefficientChange, double time) {
		
		return getSpeedFromEncoder(encoderFromCoefficient(coefficientChange), time);
		
	}
	
	
	private int getSpeedFromEncoder(int encoderDistance, double time) {
		
		time /= 1000;
		double encodePerSec = encoderDistance / time;
		
		return (int) (encodePerSec / encoderPerSecPerUnit);
		
	}
	
	public int encoderFromCoefficient(double coefficient) {
		
		return encoderFromRadians(radiansFromCoefficient(coefficient));
		
	}
	
	private double radiansFromCoefficient(double coefficient) {
		
		int range = maxAngleLimit - minAngleLimit;
		coefficient += 1;
		coefficient /= 2;
		double deg = range * coefficient;
		return Math.toRadians(deg);
		
	}
	
	private int encoderFromRadians(double radians) {
		
		return (int) (radians * getMotorMaxEncoder() / Math.toRadians(getMotorMaxAngle()));
		
	}
	
	private int getMotorMaxEncoder() {
		
		switch(this.motorType) {
		
		case MOTOR_AX12A:
			return 1024;
			
		case MOTOR_MX28T:
			return 4096;
			
		default:
			return 1024;
		}
		
	}
	
	private int getMotorMaxAngle() {
		
		switch(this.motorType) {
		
		case MOTOR_AX12A:
			return 300;
			
		case MOTOR_MX28T:
			return 360;
			
		default:
			return 300;
		}
		
	}
	
}
