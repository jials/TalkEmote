package distance;

/**
 * Created by workshop on 9/18/2015.
 */
public class Euclidean {
	private static Euclidean _euclidean = null;
	
	private Euclidean() {
	}
	
	public static Euclidean getObject() {
		if (_euclidean == null) {
			_euclidean = new Euclidean();
		}
		return _euclidean;
	}
	
    public double getDistance(double[] query1, double[] query2){
        return getNotNormalizedDistance(query1, query2);
    }

	/**
	 * @param query1
	 * @param query2
	 * @return
	 */
	private double getNotNormalizedDistance(double[] query1, double[] query2) {
		if (query1.length != query2.length){
            System.err.println("Euclidean: The dimension of the two vectors does not match!");

            System.exit(1);
        }
    	
       // distance^2 = (x1 - y1)^2 + ... + (xn - yn)^2
        
        double distance = 0;
        
        for (int i = 0; i < query1.length; i++) {
        	double difference = query1[i] - query2[i];
        	distance += difference * difference;
        }
        
        distance = Math.sqrt(distance);
        
        return distance;
	}
}
