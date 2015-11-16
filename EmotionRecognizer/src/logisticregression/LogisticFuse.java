package logisticregression;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Vector;

import signal.WaveIO;
import feature.MFCC;

public class LogisticFuse {
	public static final String[] EMOTION_TORONTO_TAGS = { 
		"angry", 
		"disgust", 
		"fear",
		"happy", 
		"neutral", 
		"sad", 
		"ps" 
	};

	public static final String[] EMOTION_IEMOCAP_TAGS = {
		"excited",
		"surprise",
		"fear",
		"happy",
		"sad",
		"frustration",
		"angry", 	
		"neutral",
		"disgust"
	};
	public static final String FILEPATH_IEMOCAP_SEGMENT = "data/input/IEMOCAP_segment/";
	public static final String FILEPATH_TORRONTO_TRAIN = "data/input/EmotionSpeechDatabase_Toronto";
	
	public static final String FILEPATH_FEATURE_OUT = "data/feature";
	public static final String EMOTION_TORRONTO_MFCC = FILEPATH_FEATURE_OUT + "/emotion_torronto_mfcc.txt";
	public static final String EMOTION_IEMOCAP_MFCC = FILEPATH_FEATURE_OUT + "/emotion_iemocap_mfcc.txt";
	
	private String[] _tags = null;
	private LogisticMulticlassClassifier _classifier = null;
	private LogisticMulticlassTrain _train = null;
	
	public LogisticFuse(String[] tags) {
		_tags = tags;
	}
	
	public boolean trainClassifier(String filePath, String trainfile) {
		File dir = new File(filePath);
		return trainClassifier(dir, trainfile);
	}
	
	public boolean trainClassifier(File datapath, String trainfile) {
		File[] datas = datapath.listFiles();
		return trainClassifier(datas, trainfile);
	}
	
	public boolean trainClassifier(File[] datas, String trainfile) {
		if (!computeMFCC(datas, trainfile)) {
			return false;
		}
		_train = new LogisticMulticlassTrain(_tags, trainfile);
		
		if (!_train.train()) {
			return false;
		}
		
		_classifier = new LogisticMulticlassClassifier(_tags);
		
		return true;
	}
	
	public void easyCrossValidation(File datapath, String trainfile) {
		File[] files = datapath.listFiles();
		int numOfTestFiles = files.length / 10;
		File[] trainFiles = Arrays.copyOfRange(files, 0, files.length - numOfTestFiles);
		File[] testFiles = Arrays.copyOfRange(files, files.length - numOfTestFiles, files.length);
		
		trainClassifier(trainFiles, trainfile);
		
		int total = testFiles.length;
		int correct = 0;
		
		for (int i = 0; i < testFiles.length; i++) {
			String audioPath = testFiles[i].getAbsolutePath();
			Vector <String> emotions = classifyEmotion(audioPath);
			
			String emotion = emotions.get(0);
			
			if (audioPath.endsWith(emotion + ".wav")) {
				correct++;
			}
		}
		double accuracy = (double) correct / (double) total;
		System.out.println(accuracy);
	}
	
	/**
	 * if model already exists, we could directly call this method
	 * @return
	 */
	public boolean trainClassifier() {
		_classifier = new LogisticMulticlassClassifier(_tags);
		return true;
	}
	
	public Vector<String> classifyEmotion(String audioPath) {
		double[] mean = getFeatureFromAudioFile(audioPath);
		return _classifier.classifyEmotion(mean);
	}
	
	public boolean computeMFCC(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioPath = audioFiles[i].getAbsolutePath();
			
			double[] mean = getFeatureFromAudioFile(audioPath);
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(audioFiles[i].getName());
			for (int j = 0; j < mean.length; j++) {
				buffer.append(" "); 
				buffer.append(mean[j]);
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param audioName
	 * @return
	 */
	public double[] getFeatureFromAudioFile(String audioPath) {
		WaveIO waveio = new WaveIO();
		short[] signal = waveio.readWave(audioPath);
		
		MFCC mfcc = new MFCC();
		mfcc.process(signal);
		double[] mean = mfcc.getMeanFeature();
		return mean;
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
}
