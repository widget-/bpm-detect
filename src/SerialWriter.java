import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

public class SerialWriter implements SerialPortEventListener
{

	int							hue;
//	final static double			PWM_MULT		= 1;
	boolean						errorWritten	= false;

	SerialPort					serialPort;
	/** The port we're normally going to use. */
	private static final String	PORT_NAMES[]	= { "/dev/tty.usbserial-A9007UX1", // Mac OS X
			"/dev/ttyUSB0", // Linux
			GUI.port, // Windows
												};
	/** A BufferedReader which will be fed by a InputStreamReader converting the bytes into characters making the
	 * displayed results codepage independent */
	private BufferedReader		input;
	/** The output stream to the port */
	private OutputStream		output;
	/** Milliseconds to block while waiting for port open */
	private static final int	TIME_OUT		= 2000;
	/** Default bits per second for COM port. */
	private static final int	DATA_RATE		= 128000;


	public void initialize()
	{
		CommPortIdentifier portId = null;
		@SuppressWarnings("rawtypes")
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();

		// First, Find an instance of serial port as set in PORT_NAMES.
		while (portEnum.hasMoreElements())
		{
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();
			for (String portName : PORT_NAMES)
			{
				System.out.println("Found port " + currPortId.getName());
				if (currPortId.getName().equals(portName))
				{
					portId = currPortId;
					break;
				}
			}
		}
		if (portId == null)
		{
			System.out.println("Could not find COM port.");
			return;
		}

		try
		{
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(), TIME_OUT);

			// set port parameters
			serialPort.setSerialPortParams(DATA_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
		}
		catch (Exception e)
		{
			System.err.println(e.toString());
		}
	}

	public synchronized void writeData(String data)
	{
		try
		{
			// Thread.sleep(1);
			output.write(data.getBytes());
		}
		catch (IOException e)
		{
			System.out.println(e.toString());
		}
		catch (Exception e)
		{
			if (!errorWritten)
			{
				System.out.println("\nAttempted to open port " + GUI.port + ":");
				e.printStackTrace();
				errorWritten = true;
			}
		}
	}

	public static String stringFromRGBData(int rPort, int gPort, int bPort, int rData, int gData, int bData)
	{
		int red = 4095 - (rData);
		int grn = 4095 - (gData);
		int blu = 4095 - (bData);
		if (red < 0 || red > 4095)
			System.out.println("red of " + red);
		if (grn < 0 || grn > 4095)
			System.out.println("grn of " + grn);
		if (blu < 0 || blu > 4095)
			System.out.println("grn of " + blu);
		return new String(rPort + " " + gPort + " " + bPort + " " + red + " " + grn + " " + blu + " ");
	}

	public void writeData2(int rPort, int gPort, int bPort, int rData, int gData, int bData)
	{
		String output = String.format("%04d,%04d,", rPort, rData);
		writeData(output);
		System.out.println("r" + output);
		output = String.format("%04d,%04d,", gPort, gData);
		writeData(output);
		// System.out.println("g"+output);
		// output = String.format("%04d,%04d,", bPort, bData);
		// writeData(output);
		// System.out.println("b"+output);
	}

	public void writeData2(int port, int value)
	{
		// String output = String.format("%04d,%04d,", port, value);
		// writeData(output);
		// System.out.println("out:"+output);
		String output = // String.valueOf(port) + ";" + String.valueOf(value) + ";"
		String.valueOf(port) + ";" + String.valueOf(value * GUI.LIGHT_OUTPUT_SCALE) + "\n";
		writeData(output);
		// System.out.println(output);
	}

	@Override
	public synchronized void serialEvent(SerialPortEvent arg0)
	{
		if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE)
		{
			try
			{
				String inputLine = input.readLine();
				System.out.println("Serial input: " + inputLine);
			}
			catch (Exception e)
			{
				System.out.println("Serial read error");
				System.err.println(e.toString());
			}
		}

	}

	public synchronized void close()
	{
		if (serialPort != null)
		{
			serialPort.removeEventListener();
			serialPort.close();
		}
	}

}