import java.util.concurrent.LinkedBlockingQueue;

public class SerialCache implements Runnable
{


	private int[]				values	= new int[512];
	private SerialWriter		writer;

	private LinkedBlockingQueue<LightMessage>	queue	= new LinkedBlockingQueue<LightMessage>(200);

	public SerialCache(SerialWriter writer)
	{
		this.writer = writer;
	}

	public void addMessage(int port, int value)
	{
		queue.add(new LightMessage(port, value));
	}

	public void WriteData(int rPort, int gPort, int bPort, int rData, int gData, int bData)
	{
		if (rPort > 4095)
			rPort = 4095;
		if (gPort > 4095)
			gPort = 4095;
		if (bPort > 4095)
			bPort = 4095;

		if (values[rPort] == rData && values[gPort] == gData && values[bPort] == bData)
			return;
		values[rPort] = rData;
		values[gPort] = gData;
		values[bPort] = bData;

		// writer.writeData(SerialWriter.stringFromRGBData(rPort, gPort, bPort, rData, gData, bData));
		writer.writeData2(rPort, gPort, bPort, rData, gData, bData);
	}

	/*
	 * (non-Javadoc)
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run()
	{
		while (true)
		{
			if (queue.isEmpty())
			{
				try
				{
					Thread.yield();
				}
				catch (Exception e)
				{
					e.printStackTrace();
				}
			}
			else
			{
				try {
					LightMessage msg = queue.poll();
					writer.writeData2(msg.channel, msg.value);
				} catch (Exception e) {
					e.printStackTrace();
				}
				// System.out.println("[SerialCache] run() " + msg.channel + ", " + msg.value);
				//queue.remove();
			}
			//if (queue.)
		}
	}

	public void clearQueue()
	{
		queue.clear();
	}

}
