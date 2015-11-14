package evaluation;

import java.io.File;
import java.util.TreeMap;

/**
 * Created by workshop on 10/1/2015.
 */
public class Recall {

    
    private static File folder;
    private static File[] listOfTrainImages;
    private static TreeMap<String, Integer> nameToFreq;
    
    private String[] _tags = null;
    
    public Recall(String folderPath, String[] tags) {
    	folder = new File(folderPath);
    	listOfTrainImages = folder.listFiles();
    	nameToFreq = new TreeMap<String, Integer>();
    	int counter = 0;
    	
    	// Initialize the TreeMap
    	for (int i = 0; i < tags.length; i++) {
    		nameToFreq.put(tags[i], 0);
    	}
    	
    	for (int i = 0; i < listOfTrainImages.length; i++) {
    		if (listOfTrainImages[i].isFile()) {
    			String fileName = listOfTrainImages[i].getName();
    			for (int j = 0; j < tags.length; j++) {
    				if (fileName.contains(tags[j])) {
    					counter = nameToFreq.get(tags[j]) + 1;
    					nameToFreq.put(tags[j], counter);
    				}
    			}
    		}
    	}
    	
    	_tags = tags;
    }
    
    public double getRecall(String fileName, String[] generatedResults) {
		String tag = null;
        int truePositive = 0;

        for (int i = 0; i < _tags.length; i++) {
            if (fileName.contains(_tags[i])) {
                tag = _tags[i];
            }
        }
        
        for (int i = 0; i < generatedResults.length; i++) {
            if (generatedResults[i].contains(tag)) {
                truePositive++;
            }
        }
        
        int falseNegative = nameToFreq.get(tag) - truePositive;
        double recall = (double) truePositive / (double) (truePositive + falseNegative);
        
        return recall;
    }
}
