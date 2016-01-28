import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;


public class BPMDetect {

    public static final int     NUM_FLAGS       = 8;

    // public static Multimap<Double, Integer> reps = TreeMultimap.create();
    public static List<Integer> bflags          = new ArrayList<Integer>();
    public static List<Integer> mflags          = new ArrayList<Integer>();
    public static List<Integer> tflags          = new ArrayList<Integer>();
    public static double[]      bpm             = { -1, -1, -1 };
    public static double[]      confidence      = { 0, 0, 0 };

    private static double[]     shortAmplitudes = new double[3];
    private static double[]     longAmplitudes  = new double[3];

    private static int          ramp            = GUI.sampleRate / 60;
    public static double        rampRequired    = 0.35;
    private static int          rampSeperation  = ramp * 5;

    private static long         downbeatTime    = System.currentTimeMillis();

    private static SampleHistory               sh;

    public BPMDetect(SampleHistory sh) {
    }
    
    public static void setSampleSource(SampleHistory sh) {
        BPMDetect.sh = sh;
    }

    public static double detectBPM(float[] samples, int sampleRate, int sampleType) {
        for (int flagType = 0; flagType < 3; flagType++) {
            List<Integer> flags = null;
            switch (sampleType) {
                case 0:
                    flags = bflags;
                    break;
                case 1:
                    flags = mflags;
                    break;
                case 2:
                    flags = tflags;
                    break;
            }

            flags.clear();

            int crossoverSize = sampleRate * 60 / 130 * 4; // 4 beats at 130BPM
            if (crossoverSize > sampleRate * GUI.historySeconds)
                return -1; // whoops

            float max = SampleHistory.getMaxInArray(samples);
            int lastBeat = 0;
            for (int i = 1; i < samples.length; i++) {
                if ((samples[i] - samples[i - 1]) / max > rampRequired) {
                    if (i - lastBeat < rampSeperation)
                        continue;
                    else
                        lastBeat = i;
                    flags.add(i - 1);
                }
            }

            shortAmplitudes[sampleType] = 0;
            for (int i = samples.length - (int) (GUI.sampleRate * 0.3); i < samples.length - 1; i++)
                shortAmplitudes[sampleType] += samples[i];
            longAmplitudes[sampleType] = 0;
            for (int i = samples.length - GUI.sampleRate * 5; i < samples.length - 1; i++)
                longAmplitudes[sampleType] += samples[i];

            bpm[sampleType] = bpmFromFlags(sampleRate, sampleType);

            if (bpm[sampleType] > 0) {
                while (bpm[sampleType] > 180)
                    bpm[sampleType] /= 2.0;
                while (bpm[sampleType] < 90)
                    bpm[sampleType] *= 2.0;
            }
        }


        GUI.getBPM().appendBPMGuess(bpm[sampleType], confidence[sampleType]);

        return bpm[sampleType];
    }

    public static double bpmFromFlags(int sampleRate, int sampleType) {
        ArrayList<Integer> distances = new ArrayList<Integer>();
        List<Integer> flags = null;
        switch (sampleType) {
            case 0:
                flags = bflags;
                break;
            case 1:
                flags = mflags;
                break;
            case 2:
                flags = tflags;
                break;
        }

        for (int i = 0; i < flags.size(); i++)
            for (int j = i + 1; j < flags.size(); j++) {
                int distance = Math.abs(flags.get(i) - flags.get(j));
                // while (distance > (sampleRate * 60 / 180))
                // distance /= 2;
                distances.add(distance);
                // System.out.print(distance+"|");
            }
        HashMap<Integer, Double> distanceMap = new HashMap<Integer, Double>();
        for (int distance : distances) {
            for (int i = -2; i < 3; i++)
                if (distanceMap.containsKey(distance + i))
                    distanceMap.put(distance + i, distanceMap.get(distance + i) + 1 - Math.abs(i) / 4.0);
                else
                    distanceMap.put(distance + i, 1 - Math.abs(i) / 4.0);
            // System.out.print(distance+" ");
        }

        // System.out.println(distanceMap.size());
        Pair<Integer, Double> bestGuess = new Pair<Integer, Double>(-1, -1.0);
        // Pair<Integer, Double> nextBestGuess = new Pair<Integer, Double>(-1, -1.0);
        int scoreTotal = 0;
        for (Entry<Integer, Double> e : distanceMap.entrySet()) {
            if (e.getValue() > bestGuess.y) {
                // nextBestGuess = bestGuess;
                bestGuess = new Pair<Integer, Double>(e.getKey(), e.getValue());
                scoreTotal += e.getValue();
            }
            // System.out.print("K: "+e.getKey()+",V:"+e.getValue()+". ");
        }
        // System.out.println();

        // confidence[sampleType] = 15.0 / Math.min(15, flags.size()) * 100;
        // confidence[sampleType] = ((bestGuess.y / nextBestGuess.y)) * 50;
        // confidence[sampleType] = ((bestGuess.y / nextBestGuess.y));
        confidence[sampleType] = bestGuess.y / scoreTotal * 100;
        confidence[sampleType] = Math.min((confidence[sampleType] - 10) * (100f / 25), 100);
        if (flags.size() == 0)
            confidence[sampleType] = 0;
        else if (flags.size() < 10)
            confidence[sampleType] *= flags.size() / 10;
        if (bestGuess.x == 0)
            return -1;
        else {
            double bpm = sampleRate * 60.0 / (bestGuess.x - 1);
            if (bpm <= 0)
                return bpm;
            while (bpm < 70)
                bpm *= 2.0;
            while (bpm > 180)
                bpm /= 2.0;
            return bpm;
        }
    }

    public static boolean isBreakdown() {
        // if (!wasBreakdown) {
        // wasBreakdown = (shortAmplitudes[0] < (shortAmplitudes[1] + shortAmplitudes[2]) * 0.4d) && (longAmplitudes[0] < (longAmplitudes[1] + longAmplitudes[2]) * 0.4d);
        // return wasBreakdown;
        // } else {
        // wasBreakdown = (shortAmplitudes[0] < (shortAmplitudes[1] + shortAmplitudes[2]) * 0.6d) && (longAmplitudes[0] < (longAmplitudes[1] + longAmplitudes[2]) * 0.6d);
        // return wasBreakdown;
        // }
        try {
            float[] samples = sh.getFftSamples(SampleHistory.FFT_BASS);
            
            int flips = 0;
            boolean peaked = false;
            for (int i = samples.length * 2 / 3; i < samples.length; i++) {
                if (peaked) {
                    if (samples[i] < FFTer.avgBassLow) {
                        peaked = false;
                    }
                } else {
                    if (samples[i] > FFTer.avgBassHigh) {
                        peaked = true;
                        flips++;
                    }
                }
            }
            return (flips < 5);
        } catch (Exception e) {
            return true;
        }
    }

    public static long getDownbeat(float[] samples, int sampleRate) {
        Pair<Integer, Float> bestGuess = new Pair<Integer, Float>(0, 0f);
        float[] sums = new float[samples.length - sampleRate];
        for (int i = 0; i < samples.length - sampleRate; i++)
            for (int j = 0; j < sampleRate; j++)
                sums[i] += samples[i + j];
        for (int i = 0; i < samples.length - 2 * sampleRate; i++)
            if (sums[i + sampleRate] - sums[i] > bestGuess.y)
                bestGuess = new Pair<Integer, Float>(i + sampleRate, sums[i + sampleRate]);

        if (sums[bestGuess.x - sampleRate] * 5 < sums[bestGuess.x])
            if (Math.abs(downbeatTime - (System.currentTimeMillis() - (samples.length - bestGuess.x) * 1000 / sampleRate)) > 100)
                downbeatTime = System.currentTimeMillis() - (samples.length - bestGuess.x) * 1000 / sampleRate;

        return downbeatTime;
    }

    public static int getBeat() {
        double time = (System.currentTimeMillis() - downbeatTime);
        double bpm = GUI.getBPM().getBPM();

        long beat = Math.round(bpm * time / 60000);

        return (int) beat % 32 + 1;
    }

    public static double getPhase() {
        double time = (System.currentTimeMillis() - downbeatTime);
        double bpm = GUI.getBPM().getBPM();
        return (bpm * time / 60000) - Math.round(bpm * time / 60000) + 0.5d;
    }

    public static float[] filterSamplesDecay(float[] samples) {
        final int DECAY_LOOKBACK = 5;
        boolean keepGoing = true;
        int cnt = 0;
        float[] s = new float[samples.length];
        s = samples.clone();
        // for (int i = samples.length - 1; i >= 0; i--) {
        // while (keepGoing) {
        // if (cnt < DECAY_LOOKBACK) {
        // if (i - cnt > 0 && s[i-cnt] < s[i]) {
        // s[i] = s[i-cnt];
        // } else {
        // cnt++;
        // }
        // } else {
        // cnt = 0;
        // keepGoing = false;
        // }
        // }
        // keepGoing = true;
        // cnt = 0;
        // }
        for (int i = s.length - 1; i > 0; i--)
            for (int j = 0; j < 5; j++)
                if (i > 5 && s[i] < s[i - j])
                    s[i] = s[i - j];
        // s[s.length - 1] = s[s.length - 2];
        return s;
    }

    public static float[] dIntegral(float[] samples) {
        float[] s = new float[samples.length];
        s[0] = 0;
        for (int i = 1; i < s.length; i++)
            if (samples[i] - samples[i - 1] > 0)
                s[i] = samples[i] - samples[i - 1];
            else
                s[i] = 0;
        return s;
    }

    public static float[] filteredIntegral(float[] samples) {
        return dIntegral(filterSamplesDecay(samples));
    }
}
