package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;


public class Sctest {
	private String _emotion = null;
	private String _testFile = null;
	private String _modelFile = null;
	private String _answerFile = null;
	
	private double[] _weightVectors = null;
	
	//classify emotion by feature vectors
	public Sctest(String emotion, String modelFile) {
		_emotion = emotion;
		_modelFile = modelFile;
		retrieveDataFromModelFile(_modelFile);
	}
	
	//classify emotion through file
	public Sctest(String emotion, String testFile,
			String modelFile, String answerFile) {
		_emotion = emotion;
		_testFile = testFile;
		_modelFile = modelFile;
		_answerFile = answerFile;
		retrieveDataFromModelFile(_modelFile);
		initializeAnswerFile(_answerFile);
	}
	
	public String getEmotion() {
		return _emotion;
	}

	private void initializeAnswerFile(String answerFile) {
		writeToFile(answerFile, false, "");
	}

	public boolean startClassifying() {
		try {
			File file = new File(_testFile);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		       if (!processSentence(line)) {
				    br.close();
		    	   return false;
		       }
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	private boolean processSentence(String line) {
		line = line.replaceAll("\\s", " ");
		String[] words = line.split(" ");
		double[] featureVector = computeFeatureVector(words);
		
		StringBuffer ans = new StringBuffer();
		ans.append(words[0]);
		ans.append(" ");
		if (isEmotion(featureVector)) {
			ans.append(1);
		} else {
			ans.append(0);
		}
		ans.append("\n");
		
		return writeToFile(_answerFile, true, ans.toString());
	}
	
	private boolean writeToFile(String filename, boolean isAppend, String line) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename, isAppend);
			fw.write(line);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		 
		return true;
	}
	
	public boolean isEmotion(double[] featureVector) {
		if (featureVector.length != _weightVectors.length) {
			System.out.println("featureVector does not have the same dimention with weight vector");
			System.exit(-1);
		}
		
		double innerProductOfFeatureAndWeightVector = 0.0;
		for (int i = 0; i < _weightVectors.length; i++) {
			innerProductOfFeatureAndWeightVector += featureVector[i] * _weightVectors[i];
		}
		double probability = innerProductOfFeatureAndWeightVector * -1;
		probability = Math.pow(Math.E, probability);
		probability++;
		probability = 1 / probability;
		
		//System.out.println(probability);
		return probability >= 0.5;
	}
    
	private double[] computeFeatureVector(String[] words) {
        //words = removeStopWords(words);
		double[] featureVector = new double[_weightVectors.length];
		Arrays.fill(featureVector, 0);
		featureVector[0] = 1;
		
		for (int i = 0; i < words.length; i++) {
			String word = words[i];
			if (isDouble(word)) {
				featureVector[i] = Double.parseDouble(word);
			} else {
				featureVector[i] = 0;
				System.out.println("got feature token not double!");
			}
		}

		return featureVector;
	}

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

	private void retrieveDataFromModelFile(String fileName) {
		try {
			File file = new File(fileName);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    Vector <Double> weights = new Vector <Double>();
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim();
		        String[] lineTokens = line.split(" ");
		        for (int i = 0; i < lineTokens.length; i++) {
		        	double weight;
		        	if (!isDouble(lineTokens[i])) {
		        		System.out.println("some weight is not double!");
		        		weight = 0;
		        	} else {
		        		weight = Double.parseDouble(lineTokens[i].trim());
		        	}
	        		//System.out.println(weight);
			        weights.add(weight);
		        }
		    }

		    _weightVectors = new double[weights.size()];
			for (int i = 0; i < weights.size(); i++) {
				_weightVectors[i] = weights.get(i).doubleValue();
				//System.out.println(_weightVectors[i]);
			}
			br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		
		
		
	}
}
