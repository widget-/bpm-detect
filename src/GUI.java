import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Scrollbar;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import javax.media.opengl.GLCapabilities;
import javax.media.opengl.GLProfile;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.ButtonGroup;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JRadioButton;
import javax.swing.JScrollBar;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import com.jogamp.opengl.util.FPSAnimator;


@SuppressWarnings("unused")
public class GUI extends JFrame implements Runnable, ActionListener, WindowListener, AdjustmentListener, ChangeListener
{

	public static final int			sampleRate			= 500;
	public static final int			sampleWindowSize	= 44100 / sampleRate;
	public static final int			historySeconds		= 10;

	public static final int			LIGHT_MODE_AUTO		= 0;
	public static final int			LIGHT_MODE_WHITE	= 1;
	public static final int			LIGHT_MODE_FADE		= 2;
	public static final int			LIGHT_MODE_OFF		= 3;
	public static final int			LIGHT_MODE_MOSAIC	= 4;
	public static final int			LIGHT_MODE_SOLID	= 5;
	public static final int			LIGHT_MODE_CHANNEL	= 6;

	public static String			port				= "COM10";

	public static int				lightMode			= LIGHT_MODE_AUTO;
	public static int				channel				= 0;

	public static Executor			executor			= Executors.newCachedThreadPool();

	private static final long		serialVersionUID	= -3184152229525314332L;
	private Timer					timer, rawDataTimer;
	private JRadioButton			rdAuto, rdWhite, rdFade, rdOff, rdSolid, rdMosaic, rdChannel;
	JSpinner						spChannel;
	JScrollBar						sbRed;
	JScrollBar						sbGreen;
	JScrollBar						sbBlue;
	private JScrollBar				sb;
	private JScrollBar				scale;
	private JComboBox               cbProgramMode;

	public static LightCollection	lights				= LightCollection.initializeLivingRoomLights();
	public static ColorPrograms		colors				= new ColorPrograms();
	public static double			LightScale			= .9d;
	public static final int			LIGHT_OUTPUT_SCALE	= 1;

	private SerialWriter			serial;
	private SerialCache				serialCache;

	private static BPMBestGuess		bpm					= new BPMBestGuess();


	public double					dbpm				= 0;

	public long						downbeat			= 0;


	AudioCapture					recorder;

	SampleHistory					sh;

	public GUI() throws HeadlessException
	{
		System.out.println("GUI: " + Thread.currentThread().getName());
		sh = new SampleHistory(sampleRate, historySeconds);

		setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();

		// graph = new WaveformPanel(sh);
		// graph.setPreferredSize(new Dimension(600, 300));
		// graph.setDoubleBuffered(true);
		//
		// add(graph);
		//

		getContentPane().setBackground(Color.black);
		getContentPane().setForeground(Color.white);

		GLProfile glp = GLProfile.getDefault();
		GLCapabilities caps = new GLCapabilities(glp);

		caps.setSampleBuffers(true);
		caps.setNumSamples(2);
		GLCanvas canvas = new GLCanvas(caps);
		canvas.setPreferredSize(new Dimension(900, 500));
		c.gridwidth = 11;
		c.fill = GridBagConstraints.BOTH;
		c.weightx = 1;
		c.weighty = 1;
		add(canvas, c);

		sbRed = new JScrollBar(Scrollbar.VERTICAL, 0, 50, 0, 305);
		sbRed.addAdjustmentListener(this);
		sbRed.setUI(new ColoredScrollbar(Color.red, true));
		sbGreen = new JScrollBar(Scrollbar.VERTICAL, 50, 50, 0, 305);
		sbGreen.addAdjustmentListener(this);
		sbGreen.setUI(new ColoredScrollbar(Color.green, true));
		sbBlue = new JScrollBar(Scrollbar.VERTICAL, 150, 50, 0, 305);
		sbBlue.addAdjustmentListener(this);
		sbBlue.setUI(new ColoredScrollbar(Color.decode("#0077FF"), true));
		c.gridwidth = 1;
		c.fill = GridBagConstraints.VERTICAL;
		c.weightx = 0;

		add(sbRed, c);
		add(sbGreen, c);
		c.gridwidth = GridBagConstraints.REMAINDER;
		add(sbBlue, c);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle("FFT Debug");

		sb = new JScrollBar(Scrollbar.HORIZONTAL);
		sb.setValue(35);
		sb.addAdjustmentListener(this);
		sb.setUI(new ColoredScrollbar(Color.gray, false));
		c.gridwidth = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.weighty = 0;
		c.weightx = 1;
		add(sb, c);
		scale = new JScrollBar(Scrollbar.HORIZONTAL);
		scale.setValue(35);
		scale.addAdjustmentListener(this);
		scale.setUI(new ColoredScrollbar(new Color(.2f, .5f, .9f), false));
		add(scale, c);

		rdAuto = new JRadioButton("Auto");
		rdAuto.setSelected(true);
		rdAuto.addActionListener(this);
		rdAuto.setBackground(Color.black);
		rdAuto.setForeground(Color.white);
		rdFade = new JRadioButton("Fade");
		rdFade.addActionListener(this);
		rdFade.setBackground(Color.black);
		rdFade.setForeground(Color.white);
		rdWhite = new JRadioButton("White");
		rdWhite.addActionListener(this);
		rdWhite.setBackground(Color.black);
		rdWhite.setForeground(Color.white);
		rdSolid = new JRadioButton("Solid");
		rdSolid.addActionListener(this);
		rdSolid.setBackground(Color.black);
		rdSolid.setForeground(Color.white);
		rdOff = new JRadioButton("Off");
		rdOff.addActionListener(this);
		rdOff.setBackground(Color.black);
		rdOff.setForeground(Color.white);
		rdMosaic = new JRadioButton("Mosaic");
		rdMosaic.addActionListener(this);
		rdMosaic.setBackground(Color.black);
		rdMosaic.setForeground(Color.white);
		rdChannel = new JRadioButton("Channel");
		rdChannel.addActionListener(this);
		rdChannel.setBackground(Color.black);
		rdChannel.setForeground(Color.white);
		spChannel = new JSpinner(new SpinnerNumberModel(0, 0, 512, 1));
		spChannel.setBackground(Color.black);
		spChannel.setForeground(Color.white);
		spChannel.setEnabled(false);
		spChannel.addChangeListener(this);
		JTextField tf = ((JSpinner.DefaultEditor)spChannel.getEditor()).getTextField();
        tf.setBackground(Color.black); 
        tf.setForeground(Color.white);
		

        List<String> programs = new ArrayList<String>();
        programs.add("AUTO");
        for (ColorPrograms.Program p : ColorPrograms.Program.values())
            programs.add(p.name());
		cbProgramMode = new JComboBox(programs.toArray());
		cbProgramMode.setBackground(Color.black);
		cbProgramMode.setForeground(Color.white);
		cbProgramMode.addActionListener(this);
		cbProgramMode.setMaximumRowCount(50);
		cbProgramMode.setLightWeightPopupEnabled(false);

		ButtonGroup rdGroup = new ButtonGroup();
		rdGroup.add(rdAuto);
		rdGroup.add(rdFade);
		rdGroup.add(rdWhite);
		rdGroup.add(rdSolid);
		rdGroup.add(rdMosaic);
		rdGroup.add(rdOff);
		rdGroup.add(rdChannel);
		

		c.weightx = 0;
		add(rdAuto, c);
		add(rdFade, c);
		add(rdWhite, c);
		add(rdSolid, c);
		add(rdMosaic, c);
		add(rdOff, c);
		add(rdChannel, c);
		add(spChannel, c);
		add(cbProgramMode, c);

		pack();
		setLocationRelativeTo(null);
		setVisible(true);

		GLPanelRenderer r = new GLPanelRenderer(sh, this);
		canvas.addGLEventListener(r);

		FPSAnimator animator = new FPSAnimator(canvas, 58);
		animator.start();

		serial = new SerialWriter();
		try
		{
			serial.initialize();
		}
		catch (Exception e)
		{
			System.out.println("ERROR: Unable to initialize serial device.");
			System.out.println("-> Is the serial device (Arduino) plugged in?)");
		}
		serialCache = new SerialCache(serial);
		Thread t = new Thread(serialCache);
		t.start();

		timer = new Timer(40, this);
		timer.start();
		rawDataTimer = new Timer(1000, this);
		rawDataTimer.start();

		BPMDetect.setSampleSource(sh);

		/*
		 * Now, we are creating an SimpleAudioRecorder object. It contains the logic of starting and stopping the
		 * recording, reading audio data from the TargetDataLine and writing the data to a file.
		 */
		recorder = new AudioCapture(sh);
		// new Thread(recorder).start();
		executor.execute(recorder);
		// new Thread(new AudioCapture(sh)).run();
	}

	@Override
	public void actionPerformed(ActionEvent e)
	{
		if (e.getSource() == rdAuto)
			lightMode = LIGHT_MODE_AUTO;
		if (e.getSource() == rdWhite)
			lightMode = LIGHT_MODE_WHITE;
		if (e.getSource() == rdFade)
			lightMode = LIGHT_MODE_FADE;
		if (e.getSource() == rdSolid)
			lightMode = LIGHT_MODE_SOLID;
		if (e.getSource() == rdOff)
			lightMode = LIGHT_MODE_OFF;
		if (e.getSource() == rdMosaic)
			lightMode = LIGHT_MODE_MOSAIC;
		if (e.getSource() == rdChannel)
			lightMode = LIGHT_MODE_CHANNEL;

		if (lightMode == LIGHT_MODE_CHANNEL)
			spChannel.setEnabled(true);
		else
			spChannel.setEnabled(false);

		if (e.getSource() == cbProgramMode) {
		    cbProgramMode.requestFocus();
		    if (cbProgramMode.getSelectedItem() == "AUTO")
		        colors.changeToProgram(false, null);
		    else
		        colors.changeToProgram(true, ColorPrograms.Program.valueOf(cbProgramMode.getSelectedItem().toString()));
		}

		if (e.getSource() == timer)
		{
			try
			{
				dbpm = GUI.getBPM().getBPM();
				dbpm = Math.round(dbpm);


				BPMDetect.detectBPM(BPMDetect.filteredIntegral(sh.getFftSamples(SampleHistory.FFT_BASS)),
						GUI.sampleRate, 0);
				BPMDetect.detectBPM(BPMDetect.filteredIntegral(sh.getFftSamples(SampleHistory.FFT_MID)),
						GUI.sampleRate, 1);
				BPMDetect.detectBPM(BPMDetect.filteredIntegral(sh.getFftSamples(SampleHistory.FFT_TREBLE)),
						GUI.sampleRate, 2);

				downbeat = BPMDetect.getDownbeat(sh.getFftSamples(SampleHistory.FFT_BASS), GUI.sampleRate);

				int lastIndex = sh.getFftSamples(SampleHistory.FFT_BASS).length - 1;
				double currRed = (sh.getFftSamples(SampleHistory.FFT_BASS)[lastIndex] - FFTer.avgBassLow)
						/ (FFTer.avgBassHigh - FFTer.avgBassLow);
				double currGreen = (sh.getFftSamples(SampleHistory.FFT_MID)[lastIndex] - FFTer.avgMidLow)
						/ (FFTer.avgMidHigh - FFTer.avgMidLow);
				double currBlue = (sh.getFftSamples(SampleHistory.FFT_TREBLE)[lastIndex] - FFTer.avgTrebLow)
				        / (FFTer.avgTrebHigh - FFTer.avgTrebLow);
						/// SampleHistory.getMaxInArray(sh.getFftSamples(SampleHistory.FFT_TREBLE));

                currRed = Math.min(1, currRed);
                currGreen = Math.min(1, currGreen);
                currBlue = Math.min(1, currBlue);

				colors.updateParameters(BPMDetect.getBeat(), BPMDetect.getPhase(), currRed, currGreen, currBlue);
				colors.updateStaticColors(255 - sbRed.getValue(), 255 - sbGreen.getValue(), 255 - sbBlue.getValue());

				// if (lightMode == LIGHT_MODE_CHANNEL) {
				// for (int i = 0; i < 255; i ++)
				// if (i != channel)
				// serialCache.addMessage(i, 0);
				// // serialCache.WriteData(i, i+1, i+2, 0, 0, 0);
				// // serialCache.WriteData(channel, channel, channel, 4095, 4095, 4095);
				// // serial.writeData2(channel, channel, channel, 4095, 4095, 4095);
				// serialCache.addMessage(channel, 255);
				// }
				// else

				serialCache.clearQueue();
				if (lightMode != LIGHT_MODE_CHANNEL)
    				for (Light light : lights) {
    					Color c = colors.getColorFor(light);
    					serialCache.addMessage(light.portRed, c.getRed() * LIGHT_OUTPUT_SCALE);
    					serialCache.addMessage(light.portGreen, c.getGreen() * LIGHT_OUTPUT_SCALE);
    					serialCache.addMessage(light.portBlue, c.getBlue() * LIGHT_OUTPUT_SCALE);
    				}
				else
				    for (int i = 0; i < 64; i++)
				        if (channel == i)
				            serialCache.addMessage(i, 255 * LIGHT_OUTPUT_SCALE);
				        else
				            serialCache.addMessage(i, 0);

			}
			catch (Exception err)
			{
				 err.printStackTrace();
			}
		}
		if (e.getSource() == rawDataTimer)
		{
			/* Debug data for Russell */
			// System.out.println("Index\tBass\tMid\tTreble");
			// float[] bass = sh.getFftSamples(SampleHistory.FFT_BASS);
			// float[] mid = sh.getFftSamples(SampleHistory.FFT_MID);
			// float[] treble = sh.getFftSamples(SampleHistory.FFT_TREBLE);
			//
			// for (int i = 0; i < bass.length; i++) {
			// System.out.println(i+"\t"+bass[i]+"\t"+mid[i]+"\t"+treble[i]);
			// }
		}
	}

	@Override
	public void run()
	{


	}


	// private void detectBPM() {
	//
	//
	//
	// }


	public static BPMBestGuess getBPM()
	{
		return bpm;
	}


	@Override
	public void windowClosing(WindowEvent arg0)
	{
		// if (recorder != null)
		// recorder.stopRecording();
	}

	@Override
	public void windowActivated(WindowEvent e)
	{
	}

	@Override
	public void windowClosed(WindowEvent e)
	{
	}

	@Override
	public void windowDeactivated(WindowEvent e)
	{
	}

	@Override
	public void windowDeiconified(WindowEvent e)
	{
	}

	@Override
	public void windowIconified(WindowEvent e)
	{
	}

	@Override
	public void windowOpened(WindowEvent e)
	{
	}

	public static void main(String[] args)
	{
		new GUI();
	}

	@Override
	public void adjustmentValueChanged(AdjustmentEvent e)
	{
		if (e.getSource() == sb)
			BPMDetect.rampRequired = e.getValue() / 100D;
		if (e.getSource() == scale)
			GUI.LightScale = e.getValue() / 100D;

	}

	@Override
	public void stateChanged(ChangeEvent e)
	{
		if (e.getSource() == spChannel)
			channel = (int) spChannel.getValue();

	}

}
