package Feature;

import SignalProcess.Frames;


/**
 * Created by workshop on 9/18/2015.
 */
public class MagnitudeSpectrum {
    protected FFTProcess fftProcess;

    public double[] getFeature(short[] inputSignals){

        Frames frames = new Frames(inputSignals);

        double[][] frameSignal = frames.frames;
        int frameNum = frameSignal.length;
        int frameLength = frameSignal[0].length;

        double[][] magnitude = new double[frameNum][frameLength];
        double[] feature = new double[frameLength];

        for (int k = 0; k < frameNum; k++){
            fftProcess = new FFTProcess();
            double[] bin = getFeaturePerFrame(frameSignal[k]);
            magnitude[k] = bin;
        }


        for (int k = 0; k < frameLength; k++){
            double temp = 0.0;
            for (int j = 0; j < frameNum; j++){
                temp += magnitude[j][k];
            }
            feature[k] = temp / frameNum;
        }
        return feature;
    }

    protected double[] getFeaturePerFrame(double[] iFrame){
        int frameLength = iFrame.length;
        double magSpectrum[] = new double[frameLength];

        //calculate FFT for current frame;
        FFTProcess.computeFFT(iFrame);

        //calculate magnitude spectrum;
        for (int k = 0; k < frameLength; k++){
            magSpectrum[k] = Math.pow(FFTProcess.real[k]*FFTProcess.real[k] + FFTProcess.imag[k]*FFTProcess.imag[k], 0.5);
        }

        return magSpectrum;
    }
}
