import edu.emory.mathcs.jtransforms.fft.FloatFFT_1D;


public class FFTer implements Runnable { // Does FFTs

    FloatFFT_1D          fft;
    float[]              data;
    SampleHistory        sh;
    long                 lastRan             = System.nanoTime();

    final private int    bassStart           = 1;                // how many fft buckets we want to sum
    final private int    bassEnd             = 6;
    final private int    midStart            = 20;
    final private int    midEnd              = 200;
    final private int    trebleStart         = 700;
    final private int    trebleEnd           = 900;

    private final double avgWeightStrong = 0.001;
    private final double avgWeightWeak   = avgWeightStrong / 8;

    public static double avgBassLow          = 0;
    public static double avgBassHigh         = 0;
    public static double avgMidLow          = 0;
    public static double avgMidHigh         = 0;
    public static double avgTrebLow          = 0;
    public static double avgTrebHigh         = 0;

    public FFTer(FloatFFT_1D fft, float[] data, SampleHistory sh) {
        this.fft = fft;
        this.data = data.clone();
        this.sh = sh;
    }

    @Override
    public void run() {
        // System.out.println("last ran " + (System.nanoTime() - lastRan) + " ago");
        // lastRan = System.nanoTime();
        fft.realForward(data);
        float bassVal = 0, midVal = 0, trebleVal = 0;
        for (int i = bassStart; i < bassEnd; i++)
            bassVal += Math.abs(data[i]);
        for (int i = midStart; i < midEnd; i++)
            midVal += Math.abs(data[i]);
        for (int i = trebleStart; i < trebleEnd; i++)
            trebleVal += Math.abs(data[i]);

        bassVal /= bassEnd - bassStart + 100;
        midVal /= midEnd - midStart;
        trebleVal /= trebleEnd - trebleStart;
        midVal /= 4;

        if (bassVal > avgBassLow)
            avgBassLow = (1 - avgWeightWeak) * avgBassLow + (avgWeightWeak) * bassVal;
        else
            avgBassLow = (1 - avgWeightStrong) * avgBassLow + (avgWeightStrong) * bassVal;

        if (bassVal < avgBassHigh)
            avgBassHigh = (1 - avgWeightWeak) * avgBassHigh + (avgWeightWeak) * bassVal;
        else
            avgBassHigh = (1 - avgWeightStrong) * avgBassHigh + (avgWeightStrong) * bassVal;


        if (midVal > avgMidLow)
            avgMidLow = (1 - avgWeightWeak) * avgMidLow + (avgWeightWeak) * midVal;
        else
            avgMidLow = (1 - avgWeightStrong) * avgMidLow + (avgWeightStrong) * midVal;

        if (midVal < avgMidHigh)
            avgMidHigh = (1 - avgWeightWeak) * avgMidHigh + (avgWeightWeak) * midVal;
        else
            avgMidHigh = (1 - avgWeightStrong) * avgMidHigh + (avgWeightStrong) * midVal;


        if (trebleVal > avgTrebLow)
            avgTrebLow = (1 - avgWeightWeak) * avgTrebLow + (avgWeightWeak) * trebleVal;
        else
            avgTrebLow = (1 - avgWeightStrong) * avgTrebLow + (avgWeightStrong) * trebleVal;

        if (trebleVal < avgTrebHigh)
            avgTrebHigh = (1 - avgWeightWeak) * avgTrebHigh + (avgWeightWeak) * trebleVal;
        else
            avgTrebHigh = (1 - avgWeightStrong) * avgTrebHigh + (avgWeightStrong) * trebleVal;

        sh.addFftSample(bassVal, midVal, trebleVal);
    }

}
