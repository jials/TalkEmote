package Distance;

/**
 * Created by workshop on 9/18/2015.
 */
public class Cosine {

    public double getDistance(double[] query1, double[] query2){
        if (query1.length != query2.length){
            System.err.println("Cosine: The dimension of the two vectors does not match!");

            System.exit(1);
        }
        
        
        //cosine similarity = sigma(xi * yi) / ( ||X|| * ||Y|| )

        double dotProduct = 0.0;
        double magnitude1 = 0.0;
        double magnitude2 = 0.0;
        double similarity = 0.0;

        for (int i = 0; i < query1.length; i++){
            dotProduct += query1[i] * query2[i];
            magnitude1 += Math.pow(query1[i], 2);
            magnitude2 += Math.pow(query2[i], 2);
        }

        magnitude1 = Math.sqrt(magnitude1);
        magnitude2 = Math.sqrt(magnitude2);

        if (magnitude1 != 0.0 && magnitude2 != 0.0){
            similarity = dotProduct / (magnitude1 * magnitude2);
        }else {
            similarity = 0.0;
        }

        //change from -similarity to 1 - similarity
        //isn't distance = 1 - similarity?
        return 1 - similarity;
    }
}