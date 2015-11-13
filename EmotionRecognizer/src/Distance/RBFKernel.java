package Distance;

/**
 * RBFKernel is radial basis function kernel
 * can see it as a normilization of euclideance distance
 * 
 * see: https://en.wikipedia.org/wiki/Radial_basis_function_kernel
 * @author Jun
 *
 */
public class RBFKernel {
	private final static int SIGMA = 1;
	private Euclidean _euclidean = null;
	
	public RBFKernel() {
		_euclidean = Euclidean.getObject();
	}
	
	public double getDistance(double[] query1, double[] query2){
        if (query1.length != query2.length){
            System.err.println("RBFKernel: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        
        /**
         * RBFKernel(x1,x2) = exp (- euclideance distance^2 / 2 sigma) 
         */
        double power = _euclidean.getDistance(query1, query2);
        power = power * power;
        power /= -2;
        power /= SIGMA;
        
        //System.err.println(power);
        //double distance = Math.pow(1000, power);
        //System.err.println(distance);

        /**
         * max { RBFKernel(x1,x2)} = max {- euclideance distance^2 / 2 sigma}
         */
        return power;
    }
	
}
