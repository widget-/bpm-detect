import java.awt.Color;
import java.awt.Graphics;
import javax.swing.JPanel;


public class WaveformPanel extends JPanel {
    private static final long serialVersionUID = -182562858491495098L;
    
    SampleHistory sh;
    
    public WaveformPanel(SampleHistory sh) {
        this.sh = sh;
    }
    
    @Override
	public void paint(Graphics g) {
        
        super.paint(g);
        
        
        float max = sh.getMaxInArray();
        int w = getWidth();
        int h = getHeight();

        
        g.clearRect(0, 0, w, h);
        
        for (int sampleType = 3; sampleType > 0; sampleType--) {
            float[] samples = smoothSamples(sh.getFftSamples(sampleType));
            
            Color color = null;
            switch (sampleType) {
                case SampleHistory.FFT_BASS:   color = Color.RED;    break;
                case SampleHistory.FFT_MID:    color = Color.GREEN;  break;
                case SampleHistory.FFT_TREBLE: color = Color.BLUE;   break;
            }
            g.setColor(color);
            
            for (int i = 0; i < (samples.length - 1); i++) {
                int x1 = w * i / samples.length;
                int x2 = w * (i+1) / samples.length;
                int y1, y2;
                if (i == 998) {
                    y1 = h-(int)(samples[i] * h / max);
                    y2 = h-(int)(samples[i+1] * h / max);
                } else {
                    // Smoothed waveform
                        y1 = h-(int)(samples[i] * h / max);
                        y2 = h-(int)(samples[i + 1] * h / max);
                }
                g.drawLine(x1, y1, x2, y2);
            }
            if (sampleType == SampleHistory.FFT_BASS) {
                String sBPM = String.valueOf(BPMDetect.detectBPM(samples, GUI.sampleRate, SampleHistory.FFT_BASS));
                g.drawChars(sBPM.toCharArray(), 0, sBPM.length(), 25, 25);
            }

        }
        
//        g.setColor(Color.BLACK);
//        int x = w * BPMDetect.detectAt / sh.getFftSamples(SampleHistory.FFT_BASS).length;
//        g.drawLine(x, 0, x, w);
        

    }
    

    public float[] smoothSamples(float[] samples) {
        int length = samples.length;
        float result[] = new float[length];
        
        if (length < 2)
            return null;
        for (int i = 2; i < samples.length - 1; i++) 
            result[i] = (samples[i-1] + samples[i] + samples [i+1]) / 3;
        result[0] = samples[0];
        result[length - 1] = samples[length - 1];
        return result;
    }

}
