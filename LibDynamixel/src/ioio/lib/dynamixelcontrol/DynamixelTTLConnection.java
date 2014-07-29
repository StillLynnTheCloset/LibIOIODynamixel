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

import java.io.InputStream;
import java.io.OutputStream;

import android.util.Log;
import ioio.lib.api.DigitalInput;
import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.Uart;
import ioio.lib.api.exception.ConnectionLostException;

public class DynamixelTTLConnection {

	public final static int[] BAUD_RATES = {9600, 19200, 38400, 57600, 115200, 200000, 250000, 400000, 500000, 1000000};
	public final static Uart.Parity PARITY = Uart.Parity.NONE;
	public final static Uart.StopBits STOP_BITS = Uart.StopBits.ONE;

	private Uart uart;
	private IOIO ioio;
	
	public DynamixelIOIOMotor[] connectedMotors;
	DigitalOutput communicationLock;
	
	InputStream is;
	OutputStream os;
	
	int rxPin;
	int txPin;
	int comLock;
	
	public int currentBaudRate = 9600;
	
	public DynamixelTTLConnection(IOIO ioio, int rxPin, int txPin, int comLock) throws ConnectionLostException {
		
		this.rxPin = rxPin;
		this.txPin = txPin;
		this.comLock = comLock;
		this.ioio = ioio;

		Log.d("","Before initilizing uart");
		uart = createNewConnection(ioio, rxPin,txPin,comLock,currentBaudRate);
		Log.d("","after initilizing uart");
		
		is = uart.getInputStream();
		os = uart.getOutputStream();
		communicationLock = ioio.openDigitalOutput(comLock);
		
	}
	
	public DynamixelIOIOMotor createMotor(int id) {
		
		DynamixelIOIOMotor motor = new DynamixelIOIOMotor(id, is, os, communicationLock);
		motor.setTorqueEnable(1);
		return motor;
		
	}
	
	public Uart createNewConnection(IOIO ioio, int rxPin, int txPin, int comLock, int baudRate) throws ConnectionLostException {
		
		DigitalInput.Spec input = new DigitalInput.Spec(rxPin,	DigitalInput.Spec.Mode.FLOATING);
		DigitalOutput.Spec output = new DigitalOutput.Spec(txPin, DigitalOutput.Spec.Mode.OPEN_DRAIN);
		
		return ioio.openUart(input, output, baudRate, PARITY, STOP_BITS);
	}
	
	/**
	 * ONLY TO BE USED WHEN A SINGLE MOTOR IS CONNECTED
	 * This will reset the motor to its factory default settings, 
	 * This will take a while because it scans through all 65535 possible connections (256 baud rates * 256 motor ids)
	 */
	public void resetToDefault() {
		
		for (int i = 0; i < 254; i++) {
			try {
				uart.close();
				uart = createNewConnection(ioio,rxPin, txPin, comLock, 2000000 / (i + 1));
				is = uart.getInputStream();
				os = uart.getOutputStream();
				for (int j = 1; j < 253; j++) {
					
					DynamixelIOIOMotor testMotor = createMotor(j);
					Log.d("","Current baud = " + 2000000 / (i + 1) +" (" + i + ") reseting motor " + j);
					testMotor.reset();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			} catch (ConnectionLostException e) {
				Log.e("","Creating uart connection failed for baud rate " + BAUD_RATES[i]);
				continue;
			}
		}
		
	}

	public DynamixelIOIOMotor[] findAllMotors() {
		
		for (int i = 0; i < BAUD_RATES.length; i++) {
			
			try {
				uart.close();
				uart = createNewConnection(ioio,rxPin,txPin,comLock,BAUD_RATES[i]);
				is = uart.getInputStream();
				os = uart.getOutputStream();
			} catch (ConnectionLostException e) {
				Log.d("","Creating uart connection failed");
				continue;
			}
			
			for (int j = 0; j < 254; j++) {
				DynamixelIOIOMotor testMotor = createMotor(j);
				Log.d("","Current baud = " + BAUD_RATES[i] + " pinging motor " + j);
				testMotor.setLEDColor(0xFE);
				Log.d("","Color set");
			}
		}
		return null;
		
	}
	
}
