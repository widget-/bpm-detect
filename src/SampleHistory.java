
public class SampleHistory {
    

    
    public static final int FFT_BASS   = 1;
    public static final int FFT_MID    = 2;
    public static final int FFT_TREBLE = 3;

//    private final float BPM_MIN = 79.5F;
//    private final float BPM_MAX = 159F;
    


    private int fftSampleArraySize; // 10 seconds
    private float fftBassSamples[];
    private float fftMidSamples[];
    private float fftTrebleSamples[];
    private float samples[];
    
    public SampleHistory(int sampleRate, int historySeconds) {
        fftSampleArraySize = sampleRate * historySeconds;
        fftBassSamples = new float[fftSampleArraySize];
        fftMidSamples = new float[fftSampleArraySize];
        fftTrebleSamples = new float[fftSampleArraySize];
        samples = new float[1024];
    }
    

    public void addFftSample(float fftBass, float fftMid, float fftTreble) {
        for (int i = 0; i < fftSampleArraySize - 1; i++) {
            fftBassSamples[i]   =  fftBassSamples[i + 1];
            fftMidSamples[i]    =  fftMidSamples[i + 1];
            fftTrebleSamples[i] =  fftTrebleSamples[i + 1];
        }
        fftBassSamples  [fftSampleArraySize - 1] = fftBass;
        fftMidSamples   [fftSampleArraySize - 1] = fftMid;
        fftTrebleSamples[fftSampleArraySize - 1] = fftTreble;
            
    }

    public float[] getFftSamples(int type) {
        switch (type) {
            case FFT_BASS:   return fftBassSamples.clone();
            case FFT_MID:    return fftMidSamples.clone();
            case FFT_TREBLE: return fftTrebleSamples.clone();
            default:         return null;
        }
    }
    

    public float[] getSamples() {
        final float[] s = samples.clone();
        return s;
    }
    

    public float getMaxInArray() {
        return getMaxInArray(fftBassSamples);
    }


    public static float getMaxInArray(float[] array) {
        float max = 0;
        for (int i = 0; i < array.length; i++)
            if (array[i] > max)
                max = array[i];
        return max;
    }
    
    public static float getMaxInArrayDecay(float[] array) {
        float max = 0;
        for (int i = 0; i < array.length; i++)
            if (i < array.length * 3 / 4)
                if (array[i] * 0.8f + (0.2f * i / array.length) > max)
                    max = array[i];
                else if (array[i] * 0.2f + (0.75f * i / array.length) > max)
                    max = array[i];
        return max * 1.5f;
    }

}
