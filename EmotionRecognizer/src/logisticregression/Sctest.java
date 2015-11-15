package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;


public class Sctest {
	private static final int NUM_OF_ARGUMENTS = 5;
	private static final int INDEX_WORD1 = 0;
	private static final int INDEX_WORD2 = 1;
	private static final int INDEX_TEST_FILE = 2;
	private static final int INDEX_MODEL_FILE = 3;
	private static final int INDEX_ANSWER_FILE = 4;

	private String _word1 = null;
	private String _word2 = null;
	private String _testFile = null;
	private String _modelFile = null;
	private String _answerFile = null;
	
	private HashMap <String, Integer> _allFeature = null;
	private double[] _weightVectors = null;
	
	private static final String CONFUSION_START = ">>";
	private static final String CONFUSION_END = "<<";
	private static final int COLLOCATION_LENGTH_BEFORE = 2;
    private static final int COLLOCATION_LENGTH_AFTER = 2;
	
	private static final String COMMAND_SEPARATOR = ":==:";

	private static String[] STOP_WORDS = null;
	private static final String STOP_WORDS_FILE = "stopwd.txt";
	
	public Sctest(String word1, String word2, String testFile,
			String modelFile, String answerFile) {
		readStopWordsFile(STOP_WORDS_FILE);
		_word1 = word1;
		_word2 = word2;
		_testFile = testFile;
		_modelFile = modelFile;
		_answerFile = answerFile;
		_allFeature = new HashMap <String, Integer>();
		retrieveDataFromModelFile(_modelFile);
		initializeAnswerFile(_answerFile);
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
		int[] featureVector = computeFeatureVector(words);
		
		StringBuffer ans = new StringBuffer();
		ans.append(words[0]);
		ans.append(" ");
		if (isWord1(featureVector)) {
			ans.append(_word1);
		} else {
			ans.append(_word2);
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
	
	private boolean isWord1(int[] featureVector) {
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
    
	private int[] computeFeatureVector(String[] words) {
        //words = removeStopWords(words);
		int[] featureVector = new int[_weightVectors.length];
		Arrays.fill(featureVector, 0);
		featureVector[0] = 1;
		
		int confuseIndexStart = -1, confuseIndexEnd = -1;
		for (int j = 1; j < words.length; j++) {
			words[j] = words[j].toLowerCase().trim();
			String word = words[j];
			
			//System.out.println(word);

			if (_allFeature.containsKey(word)) {
				//System.out.println(word);

				Integer index = _allFeature.get(word);
				featureVector[index.intValue()] = 1;
			} else if (CONFUSION_START.equals(word) &&
					   CONFUSION_END.equals(words[j + 1].trim())) {
				confuseIndexStart = j;
				confuseIndexEnd = j + 1;

				j++;
			} 
		}

		if (confuseIndexEnd < 0 || confuseIndexStart < 0) {
			System.out.println("error!");
            System.exit(-1);
        }
        
        StringBuilder sb = new StringBuilder();
        sb.insert(0, CONFUSION_START);
		for (int j = confuseIndexStart - 1; 
             j > Math.max(confuseIndexStart - 1 - COLLOCATION_LENGTH_BEFORE, 1); j--) {
			String word = words[j].trim();

			sb.insert(0, " ");

			sb.insert(0, word);
			String collocationWord = sb.toString().trim();
			//System.out.println(collocationWord);

			if (_allFeature.containsKey(collocationWord)) {
				//System.out.println(collocationWord);

				Integer index = _allFeature.get(collocationWord);
				featureVector[index.intValue()] = 1;
			}
		}

        StringBuffer collacationWordBuffer = new StringBuffer();
        collacationWordBuffer.append(CONFUSION_END);
		for (int j = confuseIndexEnd + 1; 
             j < Math.min(confuseIndexEnd + 1 + COLLOCATION_LENGTH_AFTER, words.length - 1); j++) {
			String word = words[j].trim();
			
			collacationWordBuffer.append(" ");
			collacationWordBuffer.append(word);
			String collocationWord = collacationWordBuffer.toString().trim();
			if (_allFeature.containsKey(collocationWord)) {
				//System.out.println(collocationWord);

				Integer index = _allFeature.get(collocationWord);
				featureVector[index.intValue()] = 1;
			}
		}

		return featureVector;
	}

	private boolean readStopWordsFile(String trainFile) {
		try {
			File file = new File(trainFile);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    Vector <String> stopWords = new Vector<String>();
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim().toLowerCase();
		        stopWords.add(line);
		    }
		    br.close();

		    STOP_WORDS = new String[stopWords.size()];
		    for (int i = 0; i < stopWords.size(); i++) {
		    	STOP_WORDS[i] = stopWords.get(i);
		    }
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	private void retrieveDataFromModelFile(String fileName) {
		try {
			File file = new File(fileName);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    int index = 0;
		    Vector <Double> weights = new Vector <Double>();
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim();
		        String[] lineTokens = line.split(COMMAND_SEPARATOR);
		        String feature = lineTokens[0].trim();

		        if (_allFeature.containsKey(feature)) {
		        	System.out.println(feature);
		        }

		        _allFeature.put(feature, index);
		        double weight = Double.parseDouble(lineTokens[1].trim());
		        //System.out.println(weight);
		        weights.add(weight);
		        index++;
		    }

		    _weightVectors = new double[weights.size()];
			for (int i = 0; i < weights.size(); i++) {
				_weightVectors[i] = weights.get(i).doubleValue();
				//System.out.println(_weightVectors[i]);
			}
			br.close();
			if (_weightVectors.length != _allFeature.size()) {
				System.out.println(_weightVectors.length);
				System.out.println(_allFeature.size());
				System.out.println("number of features are not same with weight vectors");
				System.exit(-1);
			}

		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}

		
		
		
	}

	private static Sctest initSctest(String[] args) {
		String inputFormat = "Input format: java sctest word1 word2 test_file model_file answer_file";

		if (args.length < NUM_OF_ARGUMENTS) {
			System.out.println("Error: not enough arguments");
			System.out.println(inputFormat);
			System.exit(-1);
		} else if (args.length > NUM_OF_ARGUMENTS) {
			System.out.println("Error: too many arguments");
			System.out.println(inputFormat);
			System.exit(-1);
		} else {
			//correct, do ntg
		}

		String word1 = args[INDEX_WORD1];
		String word2 = args[INDEX_WORD2];
		String testFile = args[INDEX_TEST_FILE];
		String modelFile = args[INDEX_MODEL_FILE];
		String answerFile = args[INDEX_ANSWER_FILE];

		return new Sctest(word1, word2, testFile, modelFile, answerFile);
	}
    
    public static void main(String[] args) {
        long startTime = System.currentTimeMillis();

        Sctest test = initSctest(args);
        if (!test.startClassifying()) {
        	System.out.println("not clasifying");
        	System.exit(-1);
        }
        
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("total time: " + totalTime);
	}
}
