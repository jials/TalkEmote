package Distance;


public class Bhattacharyya {
	public double getDistance(double[] query1, double[] query2){
		if (query1.length != query2.length){
            System.err.println("Bhattacharyya: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        
        double h1 = 0.0;
		double h2 = 0.0;
        double Sum = 0.0;

		int N = query1.length;
        for(int i = 0; i < N; i++) {
        	h1 = h1 + query1[i];
        	h2 = h2 + query2[i];
            Sum = Sum + Math.sqrt(query1[i] * query2[i]);
        }
        
        double distance = Math.sqrt( 1 - Sum / Math.sqrt(h1 * h2));
        return distance;
        
    }
}
