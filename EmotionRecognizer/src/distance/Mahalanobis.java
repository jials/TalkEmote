package distance;

import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.stat.correlation.Covariance;

/**
 * Don't know how to get the covariance matrix
 * 
 * @author Jun
 */
public class Mahalanobis {

	
	public double getDistance(double[] query1, double[] query2, double[][] MFCC){
		if (query1.length != query2.length){
            System.err.println("Mahalanobis: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        double distance = 0;
        
        /*estimation of covariance matrix*/
        double[][] dif = new double[1][query1.length];
        for (int i = 0; i < query1.length; i++) {
	        dif[0][i] = query1[i] - query2[i];
        }
        

		RealMatrix difMatrix = MatrixUtils.createRealMatrix(MFCC);
		RealMatrix cov = new Covariance(difMatrix).getCovarianceMatrix();
		RealMatrix invCov = new LUDecomposition(cov).getSolver().getInverse();
    	
		
		
		// Left-hand side of the equation: vector * invcov^-1
        double [] left = invCov.preMultiply(dif[0]);
		
        // Compute the dot product of both vectors
        for (int i = 0; i < query1.length; i++) {
        	distance += left[i] * dif[0][i];
        }
        
		distance = Math.sqrt(distance);
        return distance;
        
    }
}
