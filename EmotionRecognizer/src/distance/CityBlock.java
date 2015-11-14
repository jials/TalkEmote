package distance;

/**
 * Created by workshop on 9/18/2015.
 */
public class CityBlock {
    public double getDistance(double[] query1, double[] query2){
        if (query1.length != query2.length){
            System.err.println("CityBlock: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        /**
         * Please implement the distance/similarity function by yourself.
         */
        
        //distance = sigma |xi - yi|
        
        double distance = 0;
        
        for (int i = 0; i < query1.length; i++) {
        	double difference = query1[i] - query2[i];
        	distance += Math.abs(difference);
        }
                
        return distance;
    }
}
