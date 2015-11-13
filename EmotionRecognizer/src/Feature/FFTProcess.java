package Feature;

/**
 * Created by workshop on 9/18/2015.
 */
public class FFTProcess {
    /**
     * number of points
     */
    protected static int numPoints;
    /**
     * real part
     */
    public static double real[];
    /**
     * imaginary part
     */
    public static double imag[];

    /**
     * performs Fast Fourier Transformation<br>
     * calls: none<br>
     * called by: featureExtraction
     * @param signal  */
    public static void computeFFT(double signal[]){
        numPoints = signal.length;

        // initialize real & imag array
        real = new double[numPoints];
        imag = new double[numPoints];

        // move the N point signal into the real part of the complex DFT's time domain
        real = signal;

        // set all of the samples in the imaginary part to zero
        for (int i = 0; i < imag.length; i++){
            imag[i] = 0;
        }

        // perform FFT using the real & imag array
        FFT();
    }

    /**
     * performs Fast Fourier Transformation<br>
     * calls: none<br>
     * called by: fft
     */
    private static void FFT(){
        if (numPoints == 1) return;

        final double pi = Math.PI;
        final int numStages = (int)(Math.log(numPoints) / Math.log(2));

        int halfNumPoints = numPoints >> 1;
        int j = halfNumPoints;

        // FFT time domain decomposition carried out by "bit reversal sorting" algorithm
        int k = 0;
        for (int i = 1; i < numPoints - 2; i++){
            if (i < j){
                // swap
                double tempReal = real[j];
                double tempImag = imag[j];
                real[j] = real[i];
                imag[j] = imag[i];
                real[i] = tempReal;
                imag[i] = tempImag;
            }

            k = halfNumPoints;

            while ( k <= j ){
                j -= k;
                k >>=1;
            }

            j += k;
        }

        // loop for each stage
        for (int stage = 1; stage <= numStages; stage++){

            int LE = 1;
            for (int i = 0; i < stage; i++)
                LE <<= 1;

            int LE2 = LE >> 1;
            double UR = 1;
            double UI = 0;

            // calculate sine & cosine values
            double SR = Math.cos( pi / LE2 );
            double SI = -Math.sin( pi / LE2 );

            // loop for each sub DFT
            for (int subDFT = 1; subDFT <= LE2; subDFT++){

                // loop for each butterfly
                for (int butterfly = subDFT - 1; butterfly <= numPoints - 1; butterfly+=LE){
                    int ip = butterfly + LE2;

                    // butterfly calculation
                    double tempReal = real[ip] * UR - imag[ip] * UI;
                    double tempImag = real[ip] * UI + imag[ip] * UR;
                    real[ip] = real[butterfly] - tempReal;
                    imag[ip] = imag[butterfly] - tempImag;
                    real[butterfly] += tempReal;
                    imag[butterfly] += tempImag;
                }

                double tempUR = UR;
                UR = tempUR * SR - UI * SI;
                UI = tempUR * SI + UI * SR;
            }
        }
    }
}
