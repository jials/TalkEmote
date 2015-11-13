package Feature;

/**
 * Created by workshop on 9/18/2015.
 */
public class ZeroCrossing {
    public ZeroCrossing(){

    }

    public double[] getFeature(short[] inputSignals){
        long count = 0;
        for (int i = 0;  i < inputSignals.length - 1; i++){
            if (inputSignals[i] > 0 && inputSignals[i + 1] < 0){
                count ++;
            }else if (inputSignals[i] < 0 && inputSignals[i + 1] > 0){
                count ++;
            }else if (inputSignals[i] == 0 && inputSignals[i+1] < 0){
                count ++;
            }
        }

        double[] result = new double[1];
        result[0] = (double)count/(double)inputSignals.length;
        return result;
    }
}
