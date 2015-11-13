package Distance;

public class Chebychev {
	public double getDistance(double[] query1, double[] query2){
		if (query1.length != query2.length){
            System.err.println("Chebychev: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        
        double distance = 0.0;

		int N = query1.length;		
        for(int i = 0; i < N; i++) {
        	double dif = Math.abs(query1[i] - query2[i]);
        	distance = Math.max(dif, distance);
        }
        
        return distance;
        
    }
}
