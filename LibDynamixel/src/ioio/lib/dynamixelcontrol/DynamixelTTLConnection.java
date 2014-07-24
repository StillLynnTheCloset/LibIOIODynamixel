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
		
		return new DynamixelIOIOMotor(id, is, os, communicationLock);
		
	}
	
	public Uart createNewConnection(IOIO ioio, int rxPin, int txPin, int comLock, int baudRate) throws ConnectionLostException {
		
		DigitalInput.Spec input = new DigitalInput.Spec(rxPin,	DigitalInput.Spec.Mode.FLOATING);
		DigitalOutput.Spec output = new DigitalOutput.Spec(txPin, DigitalOutput.Spec.Mode.OPEN_DRAIN);
		
		return ioio.openUart(input, output, baudRate, PARITY, STOP_BITS);
	}
	
	/**
	 * ONLY TO BE USED WHEN A SINGLE MOTOR IS CONNECTED
	 * This will reset the motor to its factory default settings, 
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
