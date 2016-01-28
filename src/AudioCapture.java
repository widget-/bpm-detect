import java.io.IOException;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;

public class AudioCapture implements Runnable
{
    private TargetDataLine      m_line;
    private AudioInputStream    m_audioInputStream;
    private boolean             running = true;
    private SampleHistory       sh;



    public AudioCapture(SampleHistory sh)
    {

        
        AudioFormat audioFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED,
                                                  44100.0F, 16, 2, 4, 44100.0F, false);
    
      /* Now, we are trying to get a TargetDataLine. The
         TargetDataLine is used later to read audio data from it.
         If requesting the line was successful, we are opening
         it (important!).
      */
      DataLine.Info   info = new DataLine.Info(TargetDataLine.class, audioFormat);
      System.out.println(info.toString());
      TargetDataLine  targetDataLine = null;
      try
      {
          targetDataLine = (TargetDataLine) AudioSystem.getLine(info);
          targetDataLine.open(audioFormat);
      }
      catch (LineUnavailableException e)
      {
          System.out.println("unable to get a recording line");
          e.printStackTrace();
          System.exit(1);
      }
        
        
        m_line = targetDataLine;
        m_audioInputStream = new AudioInputStream(targetDataLine);
        this.sh = sh;
        // Debug stuff
        AudioFormat format = m_audioInputStream.getFormat();
        System.out.println(format.getChannels()+" Channels.");
        System.out.println(format.getEncoding().toString()+" encoding.");
        System.out.println(format.getSampleSizeInBits()+ " sample size.");
//        this.setPriority(Thread.MAX_PRIORITY);
    }



    /** Stops the recording.
    It's not a good idea to call this method just 'stop()'
        because stop() is a (deprecated) method of the class 'Thread'.
        And we don't want to override this method.
    */
    public void stopRecording()
    {
        running = false;
        m_line.stop();
        m_line.close();
    }




    /** Main working method.
    */
    @Override
	public void run()
    {
        m_line.start();
        System.out.println("AudioCapture: " + Thread.currentThread().getName());
        
        
        byte b[] = new byte[GUI.sampleWindowSize * 4];
        float sample = 0;
        float[] samples = sh.getSamples();
        FloatFFT_1D fft = new FloatFFT_1D(1024);
        try {
            while (running) {
//            long start = System.nanoTime();
                while (m_line.available() < GUI.sampleWindowSize * 4)
                    try {
                        Thread.sleep(5);
                    } catch (InterruptedException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                m_audioInputStream.read(b);
//                System.out.println("Took " + (System.nanoTime() - start) + "ns");
                for (int i = 0; i < (1024 - GUI.sampleWindowSize); i++) 
                    samples[i] = samples[i + GUI.sampleWindowSize];
                for (int i = 0; i < GUI.sampleWindowSize; i++) {
                    sample = b[i * 4 + 1] << 8 | (255 & b[i * 4]);
                    samples[i + (1024 - GUI.sampleWindowSize)] = sample;
                }
//                new Thread(new FFTer(fft, samples, sh)).start();
                GUI.executor.execute(new FFTer(fft, samples, sh));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }








}