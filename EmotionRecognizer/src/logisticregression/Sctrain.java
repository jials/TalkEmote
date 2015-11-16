package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

public class Sctrain {


	private static final double LEARNING_RATE = 0.05;
	private static final double GRADIENT_TRESHOLD = 0.0001;
	private static final double INITIAL_WEIGHT = 0.01;

	private String _emotion = null;
	private String _trainFile = null;
	private String _modelFile = null;

	private Vector< Vector<String> > _datasets = null;
	private double[][] _featureVectors = null;
	private int[] _outcome = null;
	private double[] _weightVectors = null;


	public Sctrain(String emotion, String trainFile, String modelFile) {
		setEmotion(emotion);
		setTrainFile(trainFile);
		setModelFile(modelFile);
		_datasets = new Vector< Vector<String> >();
	}

	private void setEmotion(String emotion) {
		_emotion = emotion;
	}

	private void setTrainFile(String trainFile) {
		_trainFile = trainFile;
	}

	private void setModelFile(String modelFile) {
		_modelFile = modelFile;
	}

	private boolean retrieveDataSet(String trainFile) {
		String[] lineTokens = null;
		Vector<String> vector = new Vector<String>();
		try {
			File file = new File(trainFile);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		    	line = line.trim();
		    	if (line.isEmpty()) {
		    		continue;
		    	}
		    	
		        line = line.replaceAll("\\s", " ");
		        
		        lineTokens = line.split(" ");      
		        
		        vector.clear();
                for (int i = 0; i < lineTokens.length; i++) {
                    lineTokens[i] = lineTokens[i].toLowerCase().trim();
                    if (!lineTokens[i].isEmpty()) {
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
		_featureVectors = new double[numOfDataSets][numOfFeature];
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
		if (_datasets.size() <= 0) {
			return false;
		}
		
		int numOfDataSets = _datasets.size();
		int numOfFeatures = _datasets.get(0).size();
		initializeWeightVector(numOfFeatures);
		initializeFeatureVector(numOfDataSets, numOfFeatures);
		initializeOutcomeVector(numOfDataSets);
		
		for (int i = 0; i < numOfDataSets; i++) {
			if (_datasets.get(i).get(0).endsWith(_emotion + ".wav")) {
				_outcome[i] = 1;
			} else {
				_outcome[i] = 0;
			}
			_featureVectors[i][0] = 1;
			
			for (int j = 1; j < numOfFeatures; j++) {
				String data = _datasets.get(i).get(j);
				if (isDouble(data)) {
					_featureVectors[i][j] = Double.parseDouble(data);
				} else {
					_featureVectors[i][j] = 0;
					System.out.println("got feature token not double!");
				}
			}
		}
		return true;
	}

    private boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
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

		double xi = _featureVectors[indexOfDataSet][indexOfFeature];
		int y = _outcome[indexOfDataSet];

		double z = Math.pow(Math.E, innerProductOfWeightAndFeature);
		z += 1;
		z = 1 / z;
        
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

		computeStochasticGradientAscent();
		boolean isSucess = saveFeaturesAndWeights(_weightVectors,
												  _modelFile);
		return isSucess;
	}
	
	private boolean saveFeaturesAndWeights(double[] weightVector, String filename) {
		FileWriter fw;
		try {
			fw = new FileWriter(filename);
			for (int i = 0; i < weightVector.length; i++) {
				fw.write(weightVector[i] + " ");
			}

			fw.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		 
		return true;
	}

}
