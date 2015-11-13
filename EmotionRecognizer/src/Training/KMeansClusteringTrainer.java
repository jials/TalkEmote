package Training;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import Distance.Euclidean;
import Feature.MFCC;
import SignalProcess.WavObj;
import SignalProcess.WaveIO;

public class KMeansClusteringTrainer {
	private int _k;
	private int _dim;
	private int[] _numOfClusterPoints;
	private Vector <double[]> _meanClusterPoints;
	
	private static final int DEFAULT_K = 4; // sqrt(121/2)
	private static final int DEFAULT_DIM = 12;
	
	public static final String DIR_TRAINING_FILES = "data/input/IEMOCAP_database";
	public static final String DIR_SEGMENTED_FILES = "data/input/IEMOCAP_segment";
	public static final String FILE_MODEL = "data/feature/kmeanclusteringmodel.txt";
	
	public KMeansClusteringTrainer() {
		initClass(DEFAULT_K, DEFAULT_DIM);
	}
	
	public KMeansClusteringTrainer(int k, int dim) {
		initClass(k, dim);
	}

	/**
	 * @param k
	 * @param dim
	 */
	private void initClass(int k, int dim) {
		setK(k);
		setDim(dim);
		setClusters(k, dim);
	}
	
	private void setK(int k) {
		_k = k;
	}
	
	public int getK() {
		return _k;
	}
	
	private void setClusters(int k, int dim) {
		_numOfClusterPoints = new int[k];
		_meanClusterPoints = new Vector <double[]>();
		for (int i = 0; i < k; i++) {
			double[] initialPoint = new double[dim];
			Vector <double[]> points = new Vector < double[] >();
			for (int j = 0; j < dim; j++) {
				initialPoint[j] = 0.3 * i;
			}
			points.add(initialPoint);
			_numOfClusterPoints[i] = 1;
			_meanClusterPoints.add(initialPoint);
		}
	}
	
	public boolean train(double[] data) {
		if (data.length != _dim) {
			return false;
		}
		
		int chosenIndex = getNearestClusterIndex(data);
		updateSelectedCluster(data, chosenIndex);
		
		return true;
	}

	/**
	 * @param data
	 * @param chosenIndex
	 */
	private void updateSelectedCluster(double[] data, int chosenIndex) {
		double[] avg = new double[_dim];
		int c = _numOfClusterPoints[chosenIndex];
		_numOfClusterPoints[chosenIndex]++;
		
		double[] meanCluster = _meanClusterPoints.get(chosenIndex);
		for (int i = 0; i < _dim; i++) {
			avg[i] = (meanCluster[i] * c + data[i])/ (c + 1.0);
		}
		
		_meanClusterPoints.set(chosenIndex, avg);
	}

	/**
	 * @param data
	 * @return
	 */
	private int getNearestClusterIndex(double[] data) {
		Euclidean euc = Euclidean.getObject();
		double minDistance = 0;
		int chosenIndex = 0;
		for (int i = 0; i < _k; i++) {
			double distance = euc.getDistance(data, _meanClusterPoints.get(i));
			if (i == 0) {
				minDistance = distance;
			} else if (minDistance > distance) {
				chosenIndex = i;
				minDistance = Math.min(minDistance, distance);
			}
		}
		return chosenIndex;
	}

	public int getDim() {
		return _dim;
	}

	private void setDim(int _dim) {
		this._dim = _dim;
	}
	
	public boolean saveClustersToFile(String filename) {
		//clear file
		if (!writeToFile(filename, false, "")) {
			return false;
		}
		
		//save
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < _k; i++) {
			double[] cluster = _meanClusterPoints.get(i);
			buffer.setLength(0);
			
			for (int j = 0; j < _dim; j++) {
				buffer.append(cluster[j]);
				buffer.append(" ");
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		
		return true;
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
	
	/**
	 * @return
	 */
	public static int getFrameSizeThatApproxOneSecond() {
		int powerOfTwo = 1;
    	for (int i = 0; i < 16; i++) {
    		powerOfTwo = powerOfTwo << 1;
    	}
		return powerOfTwo;
	}
	
	public static void main (String[] args) {
		KMeansClusteringTrainer machineLearning = new KMeansClusteringTrainer();
		
		File trainingDir = new File(DIR_SEGMENTED_FILES);
		File[] trainingFiles = trainingDir.listFiles();
		
		int powerOfTwo = getFrameSizeThatApproxOneSecond();
    	
		WaveIO waveIO = new WaveIO();
    	MFCC mfcc = new MFCC(powerOfTwo);

		for (int i = 0; i < trainingFiles.length; i++) {
			System.out.println(i + ": " + trainingFiles[i].getName());
			WavObj waveObj = waveIO.constructWavObj(trainingFiles[i].getAbsolutePath());
			//waveObj.removeSignalsWithinSeconds(2);
			//Vector <short[]> signals = waveObj.splitToSignals();
			short[] signal = waveObj.getSignal();
			mfcc.process(signal);
			double[] meanMfcc = mfcc.getMeanFeature();
			if (!machineLearning.train(meanMfcc)) {
				return;
			}

		}
		
		machineLearning.saveClustersToFile(FILE_MODEL);
	}


}
