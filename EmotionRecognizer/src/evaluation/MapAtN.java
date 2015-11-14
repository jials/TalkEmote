package evaluation;

import java.io.File;
import java.util.TreeMap;

//MAP = sum of i= 1:x of (precision at i * change in recall)
/*
 * Precision at i is a percentage of correct items among first i recommendations.

	Change in recall is 1/x if item at i is correct (for every correct item), 
	otherwise zero. Let’s assume that the number of relevant items is bigger or 
	equal to x: r >= x. If not, change in recall is 1/r for each correct i instead of 1/x.
 */
public class MapAtN {
	String[] _tags = null;
	
	private File _folder;
    private File[] _listOfTrainImages;
    private TreeMap<String, Integer> _nameToFreq;
    private int _topN;
    
    public MapAtN(String folderPath, String[] tags, int n) {
    	_folder = new File(folderPath);
    	_listOfTrainImages = _folder.listFiles();
    	int counter = 0;
    	_nameToFreq = new TreeMap<String, Integer>();
    	
    	// Initialize the TreeMap
    	for (int i = 0; i < tags.length; i++) {
    		_nameToFreq.put(tags[i], 0);
    	}
    	
    	for (int i = 0; i < _listOfTrainImages.length; i++) {
    		if (_listOfTrainImages[i].isFile()) {
    			String fileName = _listOfTrainImages[i].getName();
    			for (int j = 0; j < tags.length; j++) {
    				if (fileName.contains(tags[j])) {
    					counter = _nameToFreq.get(tags[j]) + 1;
    					_nameToFreq.put(tags[j], counter);
    				}
    			}
    		}
    	}
    	
    	_tags = tags;
    	_topN = n;
    }
    
	public double getMapAtN(String fileName, String[] generatedResults) {
		String tag = null;
		
        for (int i = 0; i < _tags.length; i++) {
            if (fileName.contains(_tags[i])) {
                tag = _tags[i];
            }
        }

        int numOfCorrect = 0;
        int size = Math.min(_topN, generatedResults.length);
		int x = Math.min(size, _nameToFreq.get(tag));
		
		double avgPrecisions = 0;
		for (int i = 0; i < size; i++) {
			double changeInRecall = 0;
			if (generatedResults[i].contains(tag)) {
				numOfCorrect++;
				changeInRecall = 1 / (double) (x);
            }

			double curPrecision = (double) numOfCorrect / (i + 1);
			double avgPrecision = curPrecision * changeInRecall;
			avgPrecisions += avgPrecision;
		}

		double mapAtTOPN = avgPrecisions;

        return mapAtTOPN;
	}
}
