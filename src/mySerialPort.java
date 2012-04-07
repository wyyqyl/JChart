import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.TooManyListenersException;

import javax.comm.CommPortIdentifier;
import javax.comm.PortInUseException;
import javax.comm.SerialPort;
import javax.comm.SerialPortEvent;
import javax.comm.SerialPortEventListener;
import javax.comm.UnsupportedCommOperationException;

public class mySerialPort implements SerialPortEventListener {
    private SerialPort serialport;
    private CommPortIdentifier portID;
    private InputStream inputStream;
    private OutputStream outputStream;
    private String data;

    public void find(String s) {
	Enumeration<?> portList = CommPortIdentifier.getPortIdentifiers();
	while (portList.hasMoreElements()) {
	    CommPortIdentifier temp = (CommPortIdentifier) portList
		    .nextElement();
	    if (temp.getPortType() == CommPortIdentifier.PORT_SERIAL) {
		if (temp.getName().equals(s)) {
		    portID = temp;
		}
	    }
	}
    }

    public void open(String s) {
	find(s);
	try {
	    serialport = (SerialPort) portID.open("Accelerometer", 2000);
	} catch (PortInUseException e) {
	    System.out.println("The serial port is being used.");
	}
	try {
	    inputStream = serialport.getInputStream();
	    outputStream = serialport.getOutputStream();
	} catch (IOException e) {
	    e.printStackTrace();
	}
	try {
	    serialport.addEventListener(this);
	} catch (TooManyListenersException e) {
	    e.printStackTrace();
	}
	serialport.notifyOnDataAvailable(true);
	try {
	    serialport.setSerialPortParams(9600, SerialPort.DATABITS_8,
		    SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
	} catch (UnsupportedCommOperationException e) {
	    System.out.println("Initialize the serial port failed.");
	}
    }

    public void write(String message) {
	try {
	    outputStream.write(message.getBytes());
	} catch (IOException e) {
	    // TODO Auto-generated catch block
	    e.printStackTrace();
	}

    }

    public void serialEvent(SerialPortEvent event) {

	switch (event.getEventType()) {
	case SerialPortEvent.BI:
	    System.out.println("SerialPortEvent.BI occurred");
	case SerialPortEvent.OE:
	    System.out.println("SerialPortEvent.OE occurred");
	case SerialPortEvent.FE:
	    System.out.println("SerialPortEvent.FE occurred");
	case SerialPortEvent.PE:
	    System.out.println("SerialPortEvent.PE occurred");
	case SerialPortEvent.CD:
	    System.out.println("SerialPortEvent.CD occurred");
	case SerialPortEvent.CTS:
	    System.out.println("SerialPortEvent.CTS occurred");
	case SerialPortEvent.DSR:
	    System.out.println("SerialPortEvent.DSR occurred");
	case SerialPortEvent.RI:
	    System.out.println("SerialPortEvent.RI occurred");
	case SerialPortEvent.OUTPUT_BUFFER_EMPTY:
	    System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY occurred");
	    break;
	case SerialPortEvent.DATA_AVAILABLE:
	    // System.out.println("SerialPortEvent.DATA_AVAILABLE occurred");

	    BufferedReader bd = new BufferedReader(new InputStreamReader(
		    inputStream));
	    String line;
	    try {
		while ((line = bd.readLine()) != null) {
		    data = line;
		}
		System.out.println("EOF on the serial port");
		System.exit(0);
	    } catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	    }

	    break;
	}
    }

    public String getData() {
	return data;
    }

    public void close() {
	serialport.close();
    }
    
    public static void main(String[] args){
	mySerialPort port = new mySerialPort();
	port.open("COM3");
    }
}