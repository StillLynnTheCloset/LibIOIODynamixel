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
	//private DigitalOutput comLock;
	private int id;
	private int motorType;
	
	public int minAngle = 0;
	public int maxAngle = 300;
	private int minAngleLimit = 0;
	private int maxAngleLimit = 300;
	private double encoderPerSecPerUnit = 1.58;
	private int currentPosition = 0;
	//private int homePosition = 0xFF;
	//private int currentPosition = 0xFF;
	
	public DynamixelIOIOMotor(int id, InputStream input, OutputStream output, DigitalOutput comLock) {
		
		this.id = id;
		is = input;
		os = output;
		//this.comLock = comLock;
		
	}
	
	public void setMotorType(int motor) {
		
		this.motorType = motor;
		
	}
	
	private byte[] recieveMessage(Boolean logOutput) throws IOException, ConnectionLostException {
		
		return null;/*
		// ff ff id length error data check
		comLock.write(false);
//		int timeOut = 100;
		int first = is.read();
		int second = is.read();
		if (logOutput) Log.e("Receiving", "  ");
		if (logOutput) Log.e("Receiving", "  ");
		if (logOutput) Log.e("Receiving", "  ");
		if (logOutput) Log.e("Receiving", "Start byte " + first);
		if (logOutput) Log.e("Receiving", "Start byte " + second);
		int servoID = 0;
		if (first != 0xFF || second != 0xFF) {
			
			if (logOutput) Log.e("Receiving","Received bad start bytes");
			if (first == 0xFF) {
				if (logOutput) Log.e("Receiving", "Missed first start byte, recovered");
				servoID = second;
			}
		
		} else {
			servoID = is.read();
		}
		
		if (logOutput) Log.e("Receiving", "Servo id " + servoID);
//		if (servoID != this.id) {
//			Log.e("Receiving","recieved a message from another servo " + servoID);
//			//return null;
//		}
		int dataLen = is.read();
		if (logOutput) Log.e("Receiving", "Data lenght " + dataLen);
		int error = is.read();
		if (logOutput) Log.e("Receiving", "Error" + error);
//		if (error != 0) {
//			Log.e("Receiving","An error was returned by the motor " + error);
//		}
		
		byte[] data = new byte[dataLen - 2];
		is.read(data);
		
		for (int i = 0; i < data.length; i++) {
			if (logOutput) Log.e("Receiving","Data " + data[i]);
		}
		
		int checkSum = is.read();
		if (logOutput) Log.e("Receiving","Checksum " + checkSum);
		
		comLock.write(true);
		return data;*/
		
	}
	
	private byte generateChecksum(byte[] bytes) {
		
		int checksum = 0;
		for (int i = 0; i < bytes.length; i++) {
			checksum += bytes[i];
		}
		checksum = (~checksum);
		return (byte) checksum;
	}
	
	private byte[] sendMessage(byte[] message) throws IOException {
		
		try {
			
			Thread.sleep(30);
			os.write(message);
			
			//Thread.sleep(30);
			//is.skip(message.length);
			//recieveMessage(false);
			
			//Thread.sleep(30);
			return null;//recieveMessage(true);
			
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		} catch (ConnectionLostException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		return null;
		
	}
	
	private byte[] readAddress(int id, int address, int length) {
		
		int messageLength = 0x04;
		byte[] params = {(byte) id, (byte) length, (byte) 0x02, (byte) address, (byte) length};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length          instruction               data             checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) messageLength, (byte) 0x02, (byte) address, (byte) length, checksum };
		try {
			//comLock.write(true);
			//Thread.sleep(30);
			return sendMessage(message);
			//comLock.write(false);
			//return recieveMessage();
		} catch (IOException e) {
			e.printStackTrace();
			
		}
		Log.d("","Finished reading address");
		return null;
		
	}
	
	private byte[] writeAddress(int id, int address, int value) {
		
		
		int length = 0x04;
		byte[] params = {(byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) value};
		byte checksum = generateChecksum(params);
		
		//                         ff           ff         id         length  instruction               data             checksum
		byte[] message = {(byte) 0xFF, (byte) 0xFF, (byte) id, (byte) length, (byte) 0x03, (byte) address, (byte) value, checksum };
		try {
			//comLock.write(true);
			//Thread.sleep(30);
			sendMessage(message);
			//comLock.write(false);
			//recieveMessage();
		} catch (IOException e) {
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
			//comLock.write(true);
			//Thread.sleep(30);
			sendMessage(message);
			//comLock.write(false);
			//recieveMessage();
		} catch (IOException e) {
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
			//recieveMessage();
		} catch (IOException e) {
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
		
		if (position > 4095
				) {
			Log.e("","Tried to move to invalid position " + position);
			return;
		}
		if (position < 0) {
			Log.e("","Tried to move to invalid position " + position);
			return;
		}
		this.currentPosition = position;
		Log.d("","Move to position " + position + "    " + position % 256 + "   " + position / 256);
		writeAddress(id, 0x1E, position % 256, position / 256);
		
	}
	
	public void setSpeed(int speed) {
		
		if (speed > 1023) {
			//Log.e("","Tried to set speed to invalid speed " + speed);
			speed = 1023;
		}
		if (speed <= 0) {
			//Log.e("","Tried to move to invalid speed "+ speed);
			speed = 1;
		}
		//Log.e("","Move at speed " + speed + "    " + speed % 256 + "   " + speed / 256);
		writeAddress(id, 0x20, speed % 256, speed / 256);

		
	}
	
	public void setAcceleration(int acceleration) {
		
		if (acceleration > 254) {
			//Log.e("","Tried to set speed to invalid speed " + acceleration);
			acceleration = 254;
		}
		if (acceleration < 0) {
			//Log.e("","Tried to move to invalid speed "+ acceleration);
			return;
		}
		//Log.e("","Move at speed " + acceleration + "    " + acceleration % 256 + "   " + acceleration / 256);
		writeAddress(id, 73, acceleration);

		
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
			setMaxAngle(4095);
		} else {
			setMinAngle(0);
			setMaxAngle(0);
		}
		
	}
	
	public void moveToPositionInTime(double  newPosition, double time) {
		
		setSpeed(getSpeedFromEncoder(currentPosition - getEncoderFromCoefficient(newPosition), time));
		moveToPosition(getEncoderFromCoefficient(newPosition));
		
	}
	
	private int getSpeedFromCoeficient(double coefficient, double time) {
		
		time *= getMotorMaxEncoder();
		time /= 1000000;
		double encodePerSec = coefficient * getMotorMaxEncoder() / time;
		encodePerSec = Math.abs(encodePerSec);
		return (int) (encodePerSec / encoderPerSecPerUnit);
		
	}
	
	private int getSpeedFromEncoder(int encoderDistance, double time) {
		
		time *= getMotorMaxEncoder();
		time /= 1000000;
		double encodePerSec = encoderDistance / time;
		encodePerSec = Math.abs(encodePerSec);
		return (int) (encodePerSec / encoderPerSecPerUnit);
		
	}
	
	private int getEncoderFromCoefficient(double coefficient) {
		
		return (int) ((1 + coefficient) * getMotorMaxEncoder() / 2);
		
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
	
	public int readPosition() {
		
		byte[] data = readAddress(id, 0x24, 2);
		if (data.length != 2) {
			return -1;
		}
		return data[0] + 256 * data[1];
		
	}
	
}
