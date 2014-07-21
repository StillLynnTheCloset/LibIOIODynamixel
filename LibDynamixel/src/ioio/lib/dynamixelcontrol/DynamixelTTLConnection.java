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

	public final static int BAUD_RATE = 9600; 
	public final static int[] BAUD_RATES = {9600, 19200, 38400, 57600, 115200, 200000, 250000, 400000, 500000, 1000000};
	public final static Uart.Parity PARITY = Uart.Parity.NONE;
	public final static Uart.StopBits STOP_BITS = Uart.StopBits.ONE;
	private Uart uart;
	
	public DynamixelIOIOMotor[] connectedMotors[];
	
	InputStream is;
	OutputStream os;
	
	public int currentBaudRate = 9600;
	
	public DynamixelTTLConnection(IOIO ioio, int rxPin, int txPin, int comLock) throws ConnectionLostException {
		
		DigitalInput.Spec input = new DigitalInput.Spec(rxPin,	DigitalInput.Spec.Mode.FLOATING);
		DigitalOutput.Spec output = new DigitalOutput.Spec(txPin, DigitalOutput.Spec.Mode.OPEN_DRAIN);
		
		Log.e("","Before initilizing uart");
		uart.close();
		uart = ioio.openUart(input, output, currentBaudRate, PARITY, STOP_BITS);
		Log.e("","after initilizing uart");
		
		is = uart.getInputStream();
		os = uart.getOutputStream();
		
	}
	
	private DynamixelIOIOMotor createMotor(int id) {
		
		return null;
		
	}
	
	
	
	
	
	
	
	
	
	
	/**
	 * ONLY TO BE USED WHEN A SINGLE MOTOR IS CONNECTED
	 * This will reset the motor to its factory default settings, with one exception, the baud rate will be set to 9600
	 */
	public void resetToDefault() {
		
		
		
	}
	
	public DynamixelIOIOMotor[] findAllMotors() {
		
		
		return null;
		
	}
	
}
