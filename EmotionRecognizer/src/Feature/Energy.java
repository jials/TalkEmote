package Feature;

import SignalProcess.Frames;

/**
 * Created by workshop on 9/18/2015.
 */
public class Energy {
    public Energy(){

    }

    /***
     * get the energy feature of the audio via its array of frames;
     * @param inputSignals the signal of audio;
     * @return the energy feature;
     */
    public double[] getFeature(short[] inputSignals){
        Frames frames = new Frames(inputSignals);
        double[][] frameSignal = frames.frames;
        /**
         * get the number of frames;
         */
        int frameNum = frameSignal.length;
        /**
         * get the length of every frame;
         */
        int frameLength = frameSignal[0].length;
        /**
         * initiate the array of energy value;
         */
        double[] energyValue = new double[frameNum];

        for (int i = 0; i < frameNum; i ++){
            double sum = 0;
            for (int j = 0; j < frameLength; j++){
                sum += Math.pow(frameSignal[i][j], 2);
            }
            energyValue[i] = Math.log(sum);
        }

        return energyValue;
    }

}
