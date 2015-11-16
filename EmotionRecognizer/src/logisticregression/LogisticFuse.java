package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

import signal.WaveIO;
import feature.MFCC;

public class LogisticFuse {
	public static final String[] EMOTION_TORRONTO_TAGS = { 
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
	
	private static final String PYTHON_SPEECH_RECOGNITION_SENTIMENT = "wav_transcribe.py";

	private SurroundingWordsGenerator _gen = null;
	
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
	
	public void easyCrossValidation(String datapath, String trainfile) {
		File dir = new File(datapath);
		easyCrossValidation(dir, trainfile);
	}
	
	public void easyCrossValidation(File datapath, String trainfile) {
		File[] files = datapath.listFiles();
		int numOfTestFiles = files.length / 10;
		File[] trainFiles = Arrays.copyOfRange(files, 0, files.length - numOfTestFiles);
		File[] testFiles = Arrays.copyOfRange(files, files.length - numOfTestFiles, files.length);
		
		trainClassifier(trainFiles, trainfile);
		
		int total = testFiles.length;
		double correct = 0;
		
		for (int i = 0; i < testFiles.length; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			
			String audioPath = testFiles[i].getAbsolutePath();
			Vector <String> emotions = classifyEmotion(audioPath);
			
			String emotion = "others";
			if (!emotions.isEmpty()) {
				for (int j = 0; j < emotions.size(); j++) {
					emotion = emotions.get(j);
					if (audioPath.endsWith(emotion + ".wav")) {
						correct += 1.0 / (double)(j+1);
						break;
					}
				}
			} else {
				if (audioPath.endsWith(emotion + ".wav")) {
					correct += 1.0;
				}
			}
			//System.out.println(testFiles[i].getName() + " " + emotion);
			
		}
		double accuracy = correct / (double) total;
		System.out.println(accuracy * 100 + "%");
	}
	
	public void easyCrossValidationWithoutFeature(String datapath, String trainfile) {
		File dir = new File(datapath);
		easyCrossValidation(dir, trainfile);
	}
	
	public void easyCrossValidationWithoutFeature(File datapath, String trainfile) {
		File[] files = datapath.listFiles();
		int numOfTestFiles = files.length / 10;
		//File[] trainFiles = Arrays.copyOfRange(files, 0, files.length - numOfTestFiles);
		File[] testFiles = Arrays.copyOfRange(files, files.length - numOfTestFiles, files.length);
		
		trainClassifier();
		
		int total = testFiles.length;
		double correct = 0;
		
		for (int i = 0; i < testFiles.length; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			
			String audioPath = testFiles[i].getAbsolutePath();
			Vector <String> emotions = classifyEmotion(audioPath);
			
			String emotion = "others";
			if (!emotions.isEmpty()) {
				for (int j = 0; j < emotions.size(); j++) {
					emotion = emotions.get(j);
					if (audioPath.endsWith(emotion + ".wav")) {
						correct += 1.0 / (double)(j+1);
						break;
					}
				}
			} else {
				if (audioPath.endsWith(emotion + ".wav")) {
					correct += 1.0;
				}
			}
			//System.out.println(testFiles[i].getName() + " " + emotion);
			
		}
		double accuracy = correct / (double) total;
		System.out.println(accuracy * 100 + "%");
	}
	
	public void easyCrossValidationEarlyFuse(String datapath, String trainfile) {
		File dir = new File(datapath);
		easyCrossValidationEarlyFuse(dir, trainfile);
	}
	
	public void easyCrossValidationEarlyFuse(File datapath, String trainfile) {
		File[] files = datapath.listFiles();
		int numOfTestFiles = files.length / 10;
		File[] trainFiles = Arrays.copyOfRange(files, 0, files.length - numOfTestFiles);
		File[] testFiles = Arrays.copyOfRange(files, files.length - numOfTestFiles, files.length);
		
		System.out.println("trainClassifier");

		trainClassifierEarlyFuse(trainFiles, trainfile);
		
		int total = testFiles.length;
		double correct = 0;
		
		System.out.println("test");

		for (int i = 0; i < testFiles.length; i++) {
			
			String audioPath = testFiles[i].getAbsolutePath();
			Vector <String> emotions = classifyEmotionEarlyFuse(audioPath);
			
			String emotion = "others";
			if (!emotions.isEmpty()) {
				for (int j = 0; j < emotions.size(); j++) {
					emotion = emotions.get(j);
					if (audioPath.endsWith(emotion + ".wav")) {
						correct += 1.0 / (double)(j+1);
						break;
					}
				}
			} else {
				if (audioPath.endsWith(emotion + ".wav")) {
					correct += 1.0;
				}
			}
			System.out.println(testFiles[i].getName() + " " + emotion);
			
		}
		double accuracy = correct / (double) total;
		System.out.println(accuracy * 100 + "%");
	}
	
	public void easyCrossValidationLateFuse(File datapath, String trainfile) {
		File[] files = datapath.listFiles();
		int numOfTestFiles = files.length / 10;
		File[] trainFiles = Arrays.copyOfRange(files, 0, files.length - numOfTestFiles);
		File[] testFiles = Arrays.copyOfRange(files, files.length - numOfTestFiles, files.length);
		
		trainClassifier(trainFiles, trainfile);
		
		int total = testFiles.length;
		double correct = 0;
		
		for (int i = 0; i < testFiles.length; i++) {
			String audioPath = testFiles[i].getAbsolutePath();
			Vector <String> emotions = classifyEmotionLateFuse(audioPath);
			
			String emotion = "others";
			if (!emotions.isEmpty()) {
				for (int j = 0; j < emotions.size(); j++) {
					emotion = emotions.get(j);
					if (audioPath.endsWith(emotion + ".wav")) {
						correct += 1.0 / (double)(j+1);
						break;
					}
				}
			} else {
				if (audioPath.endsWith(emotion + ".wav")) {
					correct += 1.0;
				}
			}
			//System.out.println(testFiles[i].getName() + " " + emotion);
			
		}
		double accuracy = correct / (double) total;
		System.out.println(accuracy * 100 + "%");
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
	
	public Vector<String> classifyEmotionEarlyFuse(String audioPath) {
		if (_gen == null) {
			System.err.println("_gen is not initialized");
			System.exit(-1);
		}
		File file = new File(audioPath);
		double[] mean = getFeatureFromAudioFile(audioPath);
		double[] ling = _gen.getFeatureVector(file);
		double[] features = fuseFeatures(mean, ling);

		return _classifier.classifyEmotion(features);
	}
	
	public Vector<String> classifyEmotionLateFuse(String audioPath) {
		Vector<String> emotions =  classifyEmotion(audioPath);
		
		File originalFile = new File(audioPath);
		Path originalPath = originalFile.toPath();
		
		File testFile = new File("test.wav");
		Path testPath = testFile.toPath();
		
		try {
			Files.copy(originalPath, testPath, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			System.exit(-1);
		}
		
		HashMap<String, String> emotionMap = getEmotionMap();
		String sentiment = getSentiment();

		for (int i = emotions.size() - 1; i >= 0; i--) {
			String emotion = emotions.get(i);
			
			if (!emotionMap.get(emotion).equals(sentiment)) {
				emotions.remove(i);
			}
		}
		System.out.println(originalFile.getName() + " " + emotions.size() + " " + sentiment);
		
		return emotions;
	}

	/**
	 * @return
	 */
	public String getSentiment() {
		String sentiment = "";
		try {
			Process process = Runtime.getRuntime().exec("python " + PYTHON_SPEECH_RECOGNITION_SENTIMENT);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;	

			int linenum = 0; 
			while ((line = br.readLine()) != null) {
				if (linenum == 0) {
					//do nothing to message
				} else if (linenum == 1) {
					line = line.trim();
					sentiment = line;
					
					
				} else {
					//do nothing
				}
				linenum++;
			  //System.out.println(line);
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		return sentiment;
	}
	
	public boolean trainClassifierEarlyFuse(String filePath, String trainfile) {
		File dir = new File(filePath);
		return trainClassifierEarlyFuse(dir, trainfile);
	}
	
	public boolean trainClassifierEarlyFuse(File datapath, String trainfile) {
		File[] datas = datapath.listFiles();
		return trainClassifierEarlyFuse(datas, trainfile);
	}
	
	public boolean trainClassifierEarlyFuse(File[] datas, String trainfile) {
		if (!computeLinguisticAndMFCC(datas, trainfile)) {
			return false;
		}
		
		System.out.println("done features");
		_train = new LogisticMulticlassTrain(_tags, trainfile);
		
		if (!_train.train()) {
			return false;
		}
		
		_classifier = new LogisticMulticlassClassifier(_tags);
		
		return true;
	}
	
	public boolean trainClassifierEarlyFuseWithoutFeatures(String datapathname, String trainfile) {
		File file = new File(datapathname);
		return trainClassifierEarlyFuseWithoutFeatures(file, trainfile);
	}
	
	public boolean trainClassifierEarlyFuseWithoutFeatures(File datapath, String trainfile) {
		File[] dir = datapath.listFiles();
		return trainClassifierEarlyFuseWithoutFeatures(dir, trainfile);
	}
	
	public boolean trainClassifierEarlyFuseWithoutFeatures(File[] datas, String trainfile) {
		_train = new LogisticMulticlassTrain(_tags, trainfile);
		
		if (!_train.train()) {
			return false;
		}
		
		_classifier = new LogisticMulticlassClassifier(_tags);
		
		return true;
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
	
	public boolean computeLinguisticAndMFCC(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		
		_gen = new SurroundingWordsGenerator();
		_gen.extractTranscriptsFromFiles(audioFiles);
		
		for (int i = 0; i < audioFiles.length; i++) {
			if (i % 100 == 0) {
				System.out.println(i);
			}
			
			String audioPath = audioFiles[i].getAbsolutePath();
			
			double[] mean = getFeatureFromAudioFile(audioPath);
			double[] ling = _gen.getFeatureVector(audioFiles[i]);
			
			double[] features = fuseFeatures(mean, ling);
			//System.out.println(features.length);
			
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(audioFiles[i].getName());
			for (int j = 0; j < features.length; j++) {
				buffer.append(" "); 
				buffer.append(features[j]);
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		return true;
	}

	/**
	 * @param mean
	 * @param ling
	 * @return
	 */
	public double[] fuseFeatures(double[] mean, double[] ling) {
		double[] features = new double[mean.length + ling.length];
		for (int j = 0; j < mean.length; j++) {
			features[j] = mean[j];
		}
		for (int j = 0; j < ling.length; j++) {
			features[j + mean.length] = ling[j];
		}
		return features;
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
	
	/**
	 * @return
	 */
	public static HashMap<String, String> getEmotionMap() {
		HashMap <String, String> emotionMap = new HashMap <String, String>();
		emotionMap.put("excited", "pos");
		emotionMap.put("surprise", "pos");
		emotionMap.put("fear", "neg");
		emotionMap.put("happy", "pos");
		emotionMap.put("sad", "neg");
		emotionMap.put("frustration", "neg");
		emotionMap.put("angry", "neg");
		emotionMap.put("neutral", "neutral");
		emotionMap.put("disgust", "neg");
		emotionMap.put("ps", "true"); 
		return emotionMap;
	}
	
	public static void main (String[] args) {
		//IEMOCAP
		//System.out.println("IEMOCAP:");
		LogisticFuse iemocap = new LogisticFuse(EMOTION_IEMOCAP_TAGS);
		//iemocap.easyCrossValidation(FILEPATH_IEMOCAP_SEGMENT, EMOTION_IEMOCAP_MFCC);
		iemocap.easyCrossValidationEarlyFuse(FILEPATH_IEMOCAP_SEGMENT, EMOTION_IEMOCAP_MFCC);
		
		//TORRONTO
		//System.out.println("TORRONTO:");
		//LogisticFuse torronto = new LogisticFuse(EMOTION_TORRONTO_TAGS);
		//torronto.easyCrossValidation(FILEPATH_TORRONTO_TRAIN, EMOTION_TORRONTO_MFCC);
	}

	public void easyCrossValidationLateFuse(String filepathIemocapSegment,
			String emotionIemocapMfcc) {
		File file = new File(filepathIemocapSegment);
		easyCrossValidationLateFuse(file, emotionIemocapMfcc);
		
	}
}
