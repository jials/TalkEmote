package SignalProcess;

/**
 * Created by workshop on 9/18/2015.
 */
public class Frames {
    /**
     * Number of samples per frame
     */
    public final static int frameLength = 512;
    /**
     * Number of overlapping samples (usually 50% of frame length)
     */
    public final static int shiftInterval = frameLength / 2;
    /**
     * Pre-Emphasis Alpha (Set to 0 if no pre-emphasis should be performed)
     */
    public final static double preEmphasisAlpha = 0.95;
    /**
     * All the frames of the input signal
     */
    public double frames[][];
    /**
     * hamming window values
     */
    public double hammingWindow[];


    public Frames(short[] inputSignals){
        preProcess(inputSignals);
    }

    /***
     * takes a audio signal and returns the magnitude spectrum feature.
     * @param inputSignals audio signal;
     * @return magnitude spectrum feature list;
     */
    protected  void preProcess(short[] inputSignals){

        double outputSignal[] = preEmphasis(inputSignals);

        framing(outputSignal);
        hammingWindow();
    }


    /**
     * performs Hamming Window<br>
     * calls: none<br>
     * called by: featureExtraction
     * @return Processed frame with hamming window applied to it
     */
    protected void hammingWindow(){
        double w[] = new double[frameLength];
        for (int n = 0; n < frameLength; n++){
            w[n] = 0.54 - 0.46 * Math.cos( (2 * Math.PI * n) / (frameLength - 1) );
        }

        for (int m = 0; m < frames.length; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] *= w[n];
            }
        }
    }
    /**
     * performs Frame Blocking to break down a speech signal into frames<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param inputSignal Speech Signal (16 bit integer data)
     */
    protected void framing(double inputSignal[]){
        double numFrames = (double)inputSignal.length / (double)(frameLength - shiftInterval);

        // unconditionally round up
        if ((numFrames / (int)numFrames) != 1){
            numFrames = (int)numFrames + 1;
        }

        // use zero padding to fill up frames with not enough samples
        double paddedSignal[] = new double[(int)numFrames * frameLength];
        for (int n = 0; n < inputSignal.length; n++){
            paddedSignal[n] = inputSignal[n];
        }

        frames = new double[(int)numFrames][frameLength];

        // break down speech signal into frames with specified shift interval to create overlap
        for (int m = 0; m < numFrames; m++){
            for (int n = 0; n < frameLength; n++){
                frames[m][n] = paddedSignal[m * (frameLength - shiftInterval) + n];
            }
        }
    }
    /**
     * perform pre-emphasis to equalize amplitude of high and low frequency<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param inputSignal Speech Signal (16 bit integer data)
     * @return Speech signal after pre-emphasis (16 bit integer data)
     */
    protected double[] preEmphasis(short inputSignal[]){
        double outputSignal[] = new double[inputSignal.length];

        // apply pre-emphasis to each sample
        for (int n = 1; n < inputSignal.length; n++){
            outputSignal[n] = inputSignal[n] - preEmphasisAlpha * inputSignal[n - 1];
        }

        return outputSignal;
    }
}
