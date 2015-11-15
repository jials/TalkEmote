package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.regex.Pattern;

public class Sctrain {
	private static final int INDEX_WORD1 = 0;
	private static final int INDEX_WORD2 = 1;
	private static final int INDEX_TRAIN_FILE = 2;
	private static final int INDEX_MODEL_FILE = 3;
	private static final int NUM_OF_ARGUMENTS = 4;

	private static String[] STOP_WORDS = null;
	private static final String STOP_WORDS_FILE = "stopwd.txt";

	private static final String REGEX_PUNCTUATION = "\\p{Punct}";

	private static final String CONFUSION_START = ">>";
	private static final String CONFUSION_END = "<<";
	private static final int COLLOCATION_LENGTH_BEFORE = 2;
    private static final int COLLOCATION_LENGTH_AFTER = 2;


	private static final double LEARNING_RATE = 0.05;
	private static final double GRADIENT_TRESHOLD = 0.0001;
	private static final double INITIAL_WEIGHT = 0.01;

	private String _word1 = null;
	private String _word2 = null;
	private String _trainFile = null;
	private String _modelFile = null;

	private Vector< Vector<String> > _datasets = null;
	private int[][] _featureVectors = null;
	private int[] _outcome = null;
	private double[] _weightVectors = null;

	private HashMap <String, Integer> _surroundingWordsFeature = null;
	private HashMap <String, Integer> _collocationsFeature = null;

	public Sctrain(String word1, String word2, String trainFile, String modelFile) {
		readStopWordsFile(STOP_WORDS_FILE);
		setWord1(word1);
		setWord2(word2);
		setTrainFile(trainFile);
		setModelFile(modelFile);
		_datasets = new Vector< Vector<String> >();
		_surroundingWordsFeature = new HashMap <String, Integer>();
		_collocationsFeature = new HashMap <String, Integer>();
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

	private void setWord1(String word1) {
		_word1 = word1;
	}

	private void setWord2(String word2) {
		_word2 = word2;
	}

	private void setTrainFile(String trainFile) {
		_trainFile = trainFile;
	}

	private void setModelFile(String modelFile) {
		_modelFile = modelFile;
	}

	private boolean retrieveDataSet(String trainFile) {
		try {
			File file = new File(trainFile);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.replaceAll("\\s", " ");
		        String[] lineTokens = line.split(" ");
		        Vector<String> vector = new Vector<String>();
                for (int i = 0; i < lineTokens.length; i++) {
                    lineTokens[i] = lineTokens[i].toLowerCase().trim();
                    if (!lineTokens[i].isEmpty() 
                    //&& !isStopWord(lineTokens[i])
                    ) {
                    	vector.add(lineTokens[i]);	
                    }
                }
		        _datasets.add(vector);
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	private void initializeWeightVector(int numOfFeature) {
		_weightVectors = new double[numOfFeature];
		Arrays.fill(_weightVectors, INITIAL_WEIGHT);
	}

	private void initializeFeatureVector(int numOfDataSets, int numOfFeature) {
		_featureVectors = new int[numOfDataSets][numOfFeature];
		for (int i = 0; i < numOfDataSets; i++) {
            for (int j = 0; j < numOfFeature; j++) {
                _featureVectors[i][j] = 0;
            }
        }
	}

	private void initializeOutcomeVector(int numOfDataSets) {
		_outcome = new int[numOfDataSets];
		Arrays.fill(_outcome, 0);
	}

	private boolean retrieveFeaturesFromDataSet() {
		int index = 1;
		index = retrieveSurroundingWordsFeature(index);
		index = retrieveCollocationsFeature(index);

		boolean isSuccess = index >= 0;
		if (isSuccess) {            
			initializeWeightVector(index);
			initializeFeatureVector(_datasets.size(), index);
			initializeOutcomeVector(_datasets.size());
		} 
		return isSuccess;
	}

    boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
	private boolean isStopWord(String word) {
		word = word.trim();
		for (String stopWord : STOP_WORDS) {
			if (stopWord.equals(word)) {
				return true;
			}	
		}
		return false;
	}

	private int retrieveSurroundingWordsFeature(int index) {
		if (index < 0) {
			return index;
		}

		for (Vector <String> dataset : _datasets) {
			for (int i = 1; i < dataset.size(); i++) {
                String word = dataset.get(i);
				word = word.trim();
				word = word.toLowerCase();
				if (word.isEmpty()) {
					continue;
				} else if (Pattern.matches(REGEX_PUNCTUATION, word)) {
					continue;
				} else if (CONFUSION_START.equals(word)) {
					i += 2;
					continue;
				} else if (CONFUSION_END.equals(word)) {
					continue;
				} else if (isStopWord(word)) {
					continue;
                } else if (isDouble(word)){
                    continue;
                } else if (_surroundingWordsFeature.containsKey(word)) {
					continue;
				} else {
					_surroundingWordsFeature.put(word, index);
					index++;
				}
			}
		}

		return index;
	}

	private int retrieveCollocationsFeature(int index) {
		if (index < 0) {
			return index;
		}

		for (Vector <String> dataset : _datasets) {
			int confussionWordIndex = -1;
			for (int i = 0; i < dataset.size(); i++) {
				String token = dataset.get(i).trim();
				if (CONFUSION_START.equals(token)) {
					confussionWordIndex = i;
					break;
				}
			}

			if (confussionWordIndex < 0) {
				//error
				System.out.println("for dataset " + dataset.get(0).trim() + 
					" : confussion word\'s index is not found");
				return confussionWordIndex;
			}

            StringBuilder sb = new StringBuilder();
            sb.insert(0, CONFUSION_START);
			for (int i = confussionWordIndex - 1; 
                 i > Math.max(confussionWordIndex - 1 - COLLOCATION_LENGTH_BEFORE, 1); i--) {
				String word = dataset.get(i).trim();

				sb.insert(0, " ");
                sb.insert(0, word);
				String collocationWord = sb.toString();
				if (!_collocationsFeature.containsKey(collocationWord)) {
					_collocationsFeature.put(collocationWord, index);
					index ++;
				}
			}

			StringBuffer collacationWordBuffer = new StringBuffer();
			collacationWordBuffer.append(CONFUSION_END);
			for (int i = confussionWordIndex + 3; 
                 i < Math.min(confussionWordIndex + 3 + COLLOCATION_LENGTH_AFTER, dataset.size() - 1); i++) {
				String word = dataset.get(i).trim();
				
				collacationWordBuffer.append(" ");
				collacationWordBuffer.append(word);
				String collocationWord = collacationWordBuffer.toString();
				if (!_collocationsFeature.containsKey(collocationWord)) {
					_collocationsFeature.put(collocationWord, index);
					index ++;
				}
			}
		}
		return index;
	}

	private void computeFeatureVectorsAndOutcome() {
		for (int i = 0; i < _datasets.size(); i++) {
			Vector <String> dataset = _datasets.get(i);

			_featureVectors[i][0] = 1;
			int confuseIndexStart = -1, confuseIndexEnd = -1;
			for (int j = 0; j < dataset.size(); j++) {
				String word = dataset.get(j).trim();

				if (_surroundingWordsFeature.containsKey(word)) {
					Integer index = _surroundingWordsFeature.get(word);
					_featureVectors[i][index.intValue()] = 1;
				} else if (CONFUSION_START.equals(word) &&
						   CONFUSION_END.equals(dataset.get(j + 2).trim())) {
					confuseIndexStart = j;
					confuseIndexEnd = j + 2;

					String correctWord = dataset.get(j + 1).trim();
					if (_word1.equals(correctWord)) {
						_outcome[i] = 1;
					} else if (_word2.equals(correctWord)) {
						_outcome[i] = 0;
					} else {
						System.out.println("the guessing word is neither word 1 (" + _word1 +
							")nor 2 (" + _word2 + ")");
						System.exit(-1);
					}

					j += 2;
				} 
			}
            
            if (confuseIndexEnd < 0 || confuseIndexStart < 0) {
                System.exit(-1);
            }
            
            StringBuilder sb = new StringBuilder();
            sb.insert(0, CONFUSION_START);
			for (int j = confuseIndexStart - 1; 
                 j >= Math.max(confuseIndexStart - 1 - COLLOCATION_LENGTH_BEFORE, 1); j--) {
				String word = dataset.get(j).trim();
				sb.insert(0, " ");
				sb.insert(0, word);
				String collocationWord = sb.toString();
				if (_collocationsFeature.containsKey(collocationWord)) {
					Integer index = _collocationsFeature.get(collocationWord);
					_featureVectors[i][index.intValue()] = 1;
				}
			}

            StringBuffer collacationWordBuffer = new StringBuffer();
            collacationWordBuffer.append(CONFUSION_END);
			for (int j = confuseIndexEnd + 1; 
                 j <= Math.min(confuseIndexEnd + 1 + COLLOCATION_LENGTH_AFTER, dataset.size() - 1); j++) {
				String word = dataset.get(j).trim();
				collacationWordBuffer.append(" ");
				collacationWordBuffer.append(word);
				String collocationWord = collacationWordBuffer.toString();
				if (_collocationsFeature.containsKey(collocationWord)) {
					Integer index = _collocationsFeature.get(collocationWord);
					_featureVectors[i][index.intValue()] = 1;
				} else {
					//System.out.println(collocationWord);
				}
			}

		}
	}

    private double calculateInnerProductOfWeightAndFeature(int indexOfDataSet, double[] weightVector) {
        double innerProductOfWeightAndFeature = 0.0;
		for (int i = 0; i < weightVector.length; i++) {
			innerProductOfWeightAndFeature -= weightVector[i] * _featureVectors[indexOfDataSet][i];
		}
        return innerProductOfWeightAndFeature;
    }
    
	private double derivativeFunction(int indexOfDataSet, int indexOfFeature, 
			                          double[] weightVector, double innerProductOfWeightAndFeature) {
		/**
		 *	xi * (y - z), z = 1/(1+e^(-w x))
		 */

		int xi = _featureVectors[indexOfDataSet][indexOfFeature];
		int y = _outcome[indexOfDataSet];

		double z = Math.pow(Math.E, innerProductOfWeightAndFeature);
		z += 1;
		z = 1 / z;
        
        if (xi == 1) {
            //System.out.println(xi * (y - z));
        }
        
		return xi * (y - z);
	}

	private void computeStochasticGradientAscent() {
		int n = _weightVectors.length;
		int dataSetSize = _datasets.size();
		int indexOfDataSet = 0;
		while (true) {
            if (indexOfDataSet % 100 == 0) {
                System.out.println(indexOfDataSet);
            }

			boolean isEnded = true;

			double[] gradients = new double[n];
            
            double innerProductOfWeightAndFeature = calculateInnerProductOfWeightAndFeature(indexOfDataSet, 
                                                                                            _weightVectors);
            
			for (int i = 0; i < n; i++) {
				gradients[i] = derivativeFunction(indexOfDataSet, i, _weightVectors, innerProductOfWeightAndFeature);
                //System.out.println(gradients[i]);
				if (Math.abs(gradients[i]) > GRADIENT_TRESHOLD) {
					isEnded = false;
				}
			}

			if (isEnded) {
				break;
			}
			indexOfDataSet++;
			indexOfDataSet %= dataSetSize;

			for (int i = 0; i < n; i ++) {
				_weightVectors[i] = _weightVectors[i] + LEARNING_RATE * gradients[i];
			}
		}
	}

	@SuppressWarnings("unused")
	private void computeBatchGradientAscent() {
		int n = _weightVectors.length;
		int dataSetSize = _datasets.size();
		while (true) {
			boolean isEnded = true;

			double[] gradients = new double[n];
			for (int i = 0; i < n; i++) {
				gradients[i] = 0;
				for (int j = 0; j < dataSetSize; j++) {
                    double innerProductOfWeightAndFeature = calculateInnerProductOfWeightAndFeature(j,
                                                                                                    _weightVectors);

					gradients[i] += derivativeFunction(j, i, _weightVectors, innerProductOfWeightAndFeature);
				}
				gradients[i] /= dataSetSize;
				if (Math.abs(gradients[i]) > GRADIENT_TRESHOLD) {
					isEnded = false;
				}
			}

			if (isEnded) {
				break;
			}

			for (int i = 0; i < n; i ++) {
				_weightVectors[i] = _weightVectors[i] + LEARNING_RATE * gradients[i];
			}
		}
	}


	public boolean startTraining() {
		if (!retrieveDataSet(_trainFile)) {
			return false;
		} else if (!retrieveFeaturesFromDataSet()) {
			return false;
		} 

		computeFeatureVectorsAndOutcome();
        //printFeatureVectors();  
		computeStochasticGradientAscent();
		boolean isSucess = saveFeaturesAndWeights(_surroundingWordsFeature,
												  _collocationsFeature,
												  _weightVectors,
												  _modelFile);
		return isSucess;
	}
	
	private boolean saveFeaturesAndWeights(HashMap<String, Integer> surroundingWords, 
										   HashMap<String, Integer> collocation,
										   double[] weightVector, String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			fw.write("interception1111:==:" + weightVector[0] + "\n");
			for(Map.Entry<String, Integer> entry : surroundingWords.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();
				
				fw.write(key + ":==:" + weightVector[value.intValue()] + "\n");
			}
			
			for(Map.Entry<String, Integer> entry : collocation.entrySet()) {
				String key = entry.getKey();
				Integer value = entry.getValue();

				fw.write(key + ":==:" + weightVector[value.intValue()] + "\n");
			}
		 
			if (weightVector.length != surroundingWords.size() + collocation.size() + 1) {
				System.out.println("why?!");
			}

			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		 
		return true;
	}

	private static Sctrain initSctrain(String[] args) {
		String inputFormat = "Input format: java sctrain word1 word2 train_file model_file";

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
		String trainFile = args[INDEX_TRAIN_FILE];
		String modelFile = args[INDEX_MODEL_FILE];
		return new Sctrain(word1, word2, trainFile, modelFile);
	}

	public static void main(String[] args) {
        long startTime = System.currentTimeMillis();
        Sctrain train = initSctrain(args);
		boolean isSuccess = train.startTraining();
		
		if (!isSuccess) {
			System.exit(-1);
		}
        
        long endTime   = System.currentTimeMillis();
        long totalTime = endTime - startTime;
        System.out.println("total time: " + totalTime);
	}
}
