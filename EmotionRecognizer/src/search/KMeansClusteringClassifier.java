package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.TreeMap;
import java.util.Vector;

import signal.WavObj;
import signal.WaveIO;
import training.KMeansClusteringTrainer;
import distance.Euclidean;
import feature.MFCC;

public class KMeansClusteringClassifier {
	private int _k = 0;
	private int _dim = 0;
	private Vector <double[]> _meanClusterPoints;
	
	public static final String DIR_TESTING_FILES = "data/input/emotionTest";
	public static final String DIR_GRUNDTRUTH = "data/input/IEMOCAP_grundtruth/";
	public static final String FILE_CLUSTER_MODEL = "data/feature/clusterModel.txt";
	public static final String EXT_WAV = ".wav";
	public static final String EXT_GRUNDTRUTH = "_cat.txt";
	public static final String AVG_GRUNDTRUTH = "_overall" + EXT_GRUNDTRUTH;
	public static final String[] EVALUATORS = {"_e1", "_e2", "_e3", "_e4"};
	public static final String[] EMOTIONS = {
		"excited",
		"surprise",
		"other",
		"fear",
		"happy",
		"sad",
		"frustration",
		"angry",
		"neutral",
		"disgust"
	};
	public static final int INDEX_EMOTION_NOT_EXIST = 10;


	
	public KMeansClusteringClassifier(String filename) {
		readClusters(filename);
	}
	

	public int getK() {
		return _k;
	}

	private void readClusters(String filename) {
		_meanClusterPoints = new Vector <double[]>();
		try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            _k = 0;
            while(line != null){
            	String[] tokens = line.split(" ");

            	if (_dim == 0) {
            		_dim = tokens.length;
            	}
            	
            	double[] data = new double[_dim];
            	for (int i = 0; i < tokens.length; i++) {
            		data[i] = Double.parseDouble(tokens[i]);
            	}
            	
            	_meanClusterPoints.add(data);
            	
            	_k++;
                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }		
	}
	
	public int calculateNearestCluster(double[] data) {
		if (data.length != _dim) {
			return -1;
		}
		
		Euclidean cos = Euclidean.getObject();
		double minDistance = 0;
		int chosenIndex = 0;
		for (int i = 0; i < _k; i++) {
			double distance = cos.getDistance(data, _meanClusterPoints.get(i));
			if (i == 0) {
				minDistance = distance;
			} else if (minDistance > distance) {
				chosenIndex = i;
				minDistance = Math.min(minDistance, distance);
			}
		}
		return chosenIndex;
	}
	

	
	
	public static void main (String[] args) {
		String fileName = KMeansClusteringTrainer.FILE_MODEL;
		KMeansClusteringClassifier classifier = new KMeansClusteringClassifier(fileName);		
		
		int powerOfTwo = KMeansClusteringTrainer.getFrameSizeThatApproxOneSecond();
    	
		WaveIO waveIO = new WaveIO();
    	MFCC mfcc = new MFCC(powerOfTwo);

    	/*
    	File testingDir = new File(DIR_TESTING_FILES);
		File[] testingFiles = testingDir.listFiles();
		
		showClusters(classifier, waveIO, mfcc, testingFiles);
		*/
    	TreeMap <String, Integer> emotionIndex = new TreeMap<String, Integer>();
    	for (int i = 0; i < EMOTIONS.length; i++) {
    		emotionIndex.put(EMOTIONS[i], i);
    	}
		
    	File trainingDir = new File(DIR_TESTING_FILES);
		File[] trainingFiles = trainingDir.listFiles();
		//showClusters(classifier, waveIO, mfcc, trainingFiles, emotionIndex);
		predictCluster(classifier, waveIO, mfcc, trainingFiles, emotionIndex);
	}
	
	private static void predictCluster(KMeansClusteringClassifier classifier,
			WaveIO waveIO, MFCC mfcc, File[] files, TreeMap<String, Integer> emotionIndex) {
		int clusterToEmotion[][] = new int[classifier.getK()][EMOTIONS.length + 1];
		for (int i = 0; i < clusterToEmotion.length; i++) {
			for (int j = 0; j < clusterToEmotion[0].length; j++) {
				clusterToEmotion[i][j] = 0;
			}
		}
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			
			WavObj waveObj = waveIO.constructWavObj(files[i].getAbsolutePath());
			short[] signal = waveObj.getSignal();
			mfcc.process(signal);//13-d mfcc
			double[] meanMfcc = mfcc.getMeanFeature();
			int index = classifier.calculateNearestCluster(meanMfcc);
			
			boolean isExist = false;
			for (int j = 0; j < EMOTIONS.length; j++) {
				if (name.endsWith(EMOTIONS[j] + ".wav")) {
					isExist = true;
					clusterToEmotion[index][j]++;
				}
			}
			if (!isExist) {
				clusterToEmotion[index][EMOTIONS.length]++;
			}
		}
		
		for (int i = 0; i < clusterToEmotion.length; i++) {
			System.out.print("index: " + i + " ");

			for (int j = 0; j < clusterToEmotion[0].length; j++) {
				String emo = "Not exist";
				if (j < INDEX_EMOTION_NOT_EXIST) {
					emo = EMOTIONS[j];
				}
				System.out.print(emo + ": " + clusterToEmotion[i][j] + " ");
			}
			System.out.println("");
		}
	}

	/**
	 * @param classifier
	 * @param waveIO
	 * @param mfcc
	 * @param files
	 * @param emotionIndex 
	 * @deprecated
	 */
	@SuppressWarnings("unused")
	private static void showClusters(KMeansClusteringClassifier classifier,
			WaveIO waveIO, MFCC mfcc, File[] files, TreeMap<String, Integer> emotionIndex) {
		int clusterToEmotion[][] = new int[classifier.getK()][EMOTIONS.length + 1];
		for (int i = 0; i < clusterToEmotion.length; i++) {
			for (int j = 0; j < clusterToEmotion[0].length; j++) {
				clusterToEmotion[i][j] = 0;
			}
		}
		
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			Vector < Vector <String> > grundtruthEmotions = new Vector < Vector <String> >();
			for (int j = 0; j < EVALUATORS.length; j++) {
				String grundtruthName = DIR_GRUNDTRUTH + name.replace(EXT_WAV, EVALUATORS[j] + EXT_GRUNDTRUTH);
				File file = new File(grundtruthName);
				if (file.exists()) {
					Vector <String> emotion = readEmotion(grundtruthName);
					grundtruthEmotions.add(emotion);
				}
			}
			
			WavObj waveObj = waveIO.constructWavObj(files[i].getAbsolutePath());
			waveObj.removeSignalsWithinSeconds(2);
			Vector <short[]> signals = waveObj.splitToSignals();
			int[] clusterIndexes = new int[signals.size()];
			for (int j = 0; j < signals.size(); j++) {
				mfcc.process(signals.get(j));//13-d mfcc
				double[] meanMfcc = mfcc.getMeanFeature();
				int index = classifier.calculateNearestCluster(meanMfcc);
				clusterIndexes[j] = index;
			}
			
			for (int k = 0; k < Math.min(clusterIndexes.length, grundtruthEmotions.get(0).size()); k++) {
				int[] temp = new int[EMOTIONS.length + 1];
				for (int j = 0; j < temp.length; j++) {
					temp[j] = 0;
				}
				for (int j = 0; j < grundtruthEmotions.size(); j++) {
					Vector <String> emotions = grundtruthEmotions.get(j);
					String emotion = emotions.get(k);
					int emotionIdx = INDEX_EMOTION_NOT_EXIST;
					if (emotionIndex.containsKey(emotion)) {
						emotionIdx = emotionIndex.get(emotion);
					}
					temp[emotionIdx]++;
				}
				int max = temp[0];
				int chosenIndex = 0;
				for (int j = 1; j < temp.length; j++) {
					if (max < temp[j]) {
						max = temp[j];
						chosenIndex = j;
					}
				}
				clusterToEmotion[clusterIndexes[k]][chosenIndex]++;

			} 
		}
		
		for (int i = 0; i < clusterToEmotion.length; i++) {
			System.out.print("index: " + i + " ");

			for (int j = 0; j < clusterToEmotion[0].length; j++) {
				String emo = "Not exist";
				if (j < INDEX_EMOTION_NOT_EXIST) {
					emo = EMOTIONS[j];
				}
				System.out.print(emo + ": " + clusterToEmotion[i][j] + " ");
			}
			System.out.println("");
		}
	}
	
	private static Vector <String> readEmotion(String filename) {
		Vector <String> emotions = new Vector <String>();
		try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
            	int beginIndex = line.indexOf(":");
            	int endIndex = line.indexOf(";");
            	String emotion = line.substring(beginIndex + 1, endIndex).trim();
            	emotions.add(emotion);
                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }		
		return emotions;
	}
}
