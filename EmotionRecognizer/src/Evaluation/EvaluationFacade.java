package Evaluation;

//audiobook

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;




import java.util.HashMap;
import java.util.Vector;

import Search.SearchDemo;
import Search.SearchDemo.Distance;
import SignalProcess.WavObj;
import SignalProcess.WaveIO;
import Tool.Stats;
import Training.AudioFeaturesGenerator;

public class EvaluationFacade {
	public static final String FILEPATH_AUDIO_TRAIN = "data/input/train";
	public static final String FILEPATH_AUDIO_TEST = "data/input/test";
	public static final String[] AUDIO_TAGS = { "bus", "busystreet", "office",
			"openairmarket", "park", "quietstreet", "restaurant",
			"supermarket", "tube", "tubestation" };

	public static final String FILEPATH_EMOTION_TRAIN = "data/input/EmotionSpeechDatabase_Toronto";
	public static final String FILEPATH_EMOTION_TEST = "data/input/emotionTest";
	public static final String[] EMOTION_TAGS = { "angry", "disgust", "fear",
			"happy", "neutral", "sad", "ps" };
	
	public static final String[] EMOTION_IEMOCAP_TAGS = {
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
	public static final int TOP_N = 20;

	private Precision _audioPrecision = null;
	private Recall _audioRecall = null;
	private MapAtN _audioMap = null;
	private Precision _emotionPrecision = null;
	private Recall _emotionRecall = null;
	private MapAtN _emotionMap = null;

	public EvaluationFacade() {
		_audioPrecision = new Precision(AUDIO_TAGS);
		_audioRecall = new Recall(FILEPATH_AUDIO_TRAIN, AUDIO_TAGS);
		_audioMap = new MapAtN(FILEPATH_AUDIO_TRAIN, AUDIO_TAGS, TOP_N);

		//_emotionPrecision = new Precision(EMOTION_TAGS);
		//_emotionRecall = new Recall(FILEPATH_EMOTION_TRAIN, EMOTION_TAGS);
		//_emotionMap = new MapAtN(FILEPATH_EMOTION_TRAIN, EMOTION_TAGS, TOP_N);
	}
	
	public static HashMap <String, String> getIemocapEmotionHashMap() {
		HashMap <String, String> emotions = new HashMap <String, String>();
		emotions.put("exc", "excited");
		emotions.put("sur", "surprise");
		emotions.put("oth", "other");
		emotions.put("fea", "fear");
		emotions.put("hap", "happy");
		emotions.put("sad", "sad");
		emotions.put("fru", "frustration");
		emotions.put("ang", "angry");
		emotions.put("neu", "neutral");
		emotions.put("dis", "disgust");
		return emotions;
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
	
	private Vector <String> retrieveEmotionFromWavFile(String wavFile) {
		if (!wavFile.endsWith(AudioFeaturesGenerator.EXT_WAV)) {
			return null;
		}
		String labelFile = wavFile.replace(AudioFeaturesGenerator.EXT_WAV, AudioFeaturesGenerator.EXT_TXT);
		String labelPath = AudioFeaturesGenerator.FILEPATH_AUDIO_IEMOCAP_LABEL + labelFile;
		
		return readIemocapLabelFile(labelPath);
	}
	
	private Vector <String> readIemocapLabelFile(String filename) {
		Vector <String> lines = new Vector <String>();
		try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
            	if (line.startsWith("[")) {
            		lines.add(line);
            	}
                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }		
		return lines;
	}
	
	public void evaluateIemocapEmotionClassification(File[] testFiles, SearchDemo search) {
		int total = 0;
		int correct = 0;
		HashMap <String, String> emotions = EvaluationFacade.getIemocapEmotionHashMap();	
		for (int i = 0; i < testFiles.length; i++) {
			String audioName = testFiles[i].getAbsolutePath();
			String audioFileName = testFiles[i].getName();
			WaveIO waveio = new WaveIO();
			WavObj obj = waveio.constructWavObj(audioName);
			
			Vector <String> lines = retrieveEmotionFromWavFile(audioFileName);
			
			
			for (int j = 0; j < lines.size(); j++) {
				String line = lines.get(j);
				line = line.substring(1).replaceAll("\\s+", " ");
				String[] tokens = line.split("]");
				String time = tokens[0].trim();
				String[] timeTokens = time.split("-");
				double startTime = Double.parseDouble(timeTokens[0].trim());
				double endTime = Double.parseDouble(timeTokens[1].trim());
				
				String nameAndEmotion = tokens[1].trim();
				String realEmotion = nameAndEmotion.split(" ")[1];
				
				if (realEmotion.equals("xxx")) {
					continue;
				}
				realEmotion = emotions.get(realEmotion).trim();
								
				short[] signal = obj.getSignalWithoutFirstFewSeconds(startTime);
				signal = obj.getSignalWithFirstFewSeconds(endTime - startTime, signal);
				
				String guessEmotion = search.classifyEmotion(signal);
				
				if (guessEmotion.equals(realEmotion)) {
					correct++;
				} else {
					System.err.println(i + " " + j);
					System.err.println(guessEmotion);
					System.err.println(realEmotion);
				}
				total++;
			}
		}
		
		double accuracy = (double) correct / (double) total;
		System.out.println(correct);
		System.out.println(total);
		System.out.println("result: " + accuracy * 100 + "%");
	}

	public void evaluateEmotionClassification(File[] testFiles, SearchDemo search) {
		int total = testFiles.length;
		int correct = 0;
		for (int i = 0; i < testFiles.length; i++) {
			String testFile = testFiles[i].getAbsolutePath();
			String emotion = search.classifyEmotion(testFile);
			System.out.println(testFiles[i].getName() + " " + emotion);
			if (emotion.equals("pleasant surprise")) {
				emotion = "ps";
			}
			
			if (testFile.contains(emotion)) {
				correct++;
			}
		}
		double accuracy = (double) correct / (double) total;
		System.out.println("result: " + accuracy * 100 + "%");
	}
	
	public boolean evaluateTest(File[] testFiles,
			ArrayList<String[]> generatedResults, String resultFile,
			String resultFileHeader, boolean isAudio) {
		writeToFile(resultFile, true, resultFileHeader);

		StringBuffer lineBuffer = new StringBuffer();

		double[] precisions = new double[testFiles.length];
		double[] recalls = new double[testFiles.length];
		double[] maps = new double[testFiles.length];
		
		
		for (int i = 0; i < testFiles.length; i++) {
			lineBuffer.setLength(0);
			String testFile = testFiles[i].getAbsolutePath();
			String[] generatedResult = generatedResults.get(i);

			if (isAudio) {
				precisions[i] = _audioPrecision.getPrecision(testFile,
						generatedResult);
				recalls[i] = _audioRecall.getRecall(testFile, generatedResult);
				maps[i] = _audioMap.getMapAtN(testFile, generatedResult);
			} else {
				precisions[i] = _emotionPrecision.getPrecision(testFile,
						generatedResult);
				recalls[i] = _emotionRecall.getRecall(testFile, generatedResult);
				maps[i] = _emotionMap.getMapAtN(testFile, generatedResult);
			}

//			lineBuffer.append(testFile);
//			lineBuffer.append(" precision: ");
//			lineBuffer.append(precisions[i]);
//			lineBuffer.append(" recall: ");
//			lineBuffer.append(recalls[i]);
//			lineBuffer.append(" map: ");
//			lineBuffer.append(maps[i]);
//			lineBuffer.append(" resultList: ");
//			for (int j = 0; j < generatedResult.length; j++) {
//				lineBuffer.append(generatedResult[j]);
//				lineBuffer.append(" ");
//			}
//			lineBuffer.append("\n");
//
//			if (!writeToFile(resultFile, true, lineBuffer.toString())) {
//				return false;
//			}
		}
		
		double avgPrecision = Stats.mean(precisions);
		double avgRecall = Stats.mean(recalls);
		double avgMapAtN = Stats.mean(maps);
		double varPrecision = Stats.variance(precisions);
		double varRecall = Stats.variance(recalls);
		double varMapAtN = Stats.variance(maps);
		lineBuffer.setLength(0);
		lineBuffer.append("precision: (average) ");
		lineBuffer.append(avgPrecision);
		lineBuffer.append(" (variance) ");
		lineBuffer.append(varPrecision);
		lineBuffer.append(" recall: (average) ");
		lineBuffer.append(avgRecall);
		lineBuffer.append(" (variance) ");
		lineBuffer.append(varRecall);
		lineBuffer.append(" map@N: (average) ");
		lineBuffer.append(avgMapAtN);
		lineBuffer.append(" (variance) ");
		lineBuffer.append(varMapAtN);
		lineBuffer.append("\n\n\n");
		return writeToFile(resultFile, true, lineBuffer.toString());
	}

	
	public static void main (String[] args) {
		//File audioTestDir = new File(FILEPATH_AUDIO_TEST);
		
		SearchDemo search = new SearchDemo();
		EvaluationFacade evaluation = new EvaluationFacade();
		File emotionTestDir = new File(FILEPATH_EMOTION_TEST);
		File[] emotionTestFiles = emotionTestDir.listFiles();
		//evaluation.evaluateIemocapEmotionClassification(emotionTestFiles, search);
		evaluation.evaluateEmotionClassification(emotionTestFiles, search);
		
		
		/*
		File emotionTestDir = new File(FILEPATH_EMOTION_TEST);
		File[] emotionTestFiles = emotionTestDir.listFiles();
		ArrayList<String[]> generatedResults = new ArrayList<String[]>();
		
		for (int i = 0; i < emotionTestFiles.length; i++) {
			String filename = emotionTestFiles[i].getAbsolutePath();
			//ArrayList <String> generatedList = search.resultListOfSpectrum(filename, false);
			ArrayList <String> generatedList = search.resultListOfMfcc(filename, false);
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		String resultFile = "result.txt";
		String resultFileHeader = "###Emotion Test Result####\n";
		boolean isAudio = false;
		
		EvaluationFacade evaluation = new EvaluationFacade();
		evaluation.evaluateTest(emotionTestFiles, generatedResults, resultFile, resultFileHeader, isAudio);
		*/
		
		/*
		// MFCC - BHAT
		File testDir = new File(FILEPATH_AUDIO_TEST);
		File[] testFiles = testDir.listFiles();
		boolean isAudio = true;
//		ArrayList<String[]> generatedResults = new ArrayList<String[]>();
		
//		for (int i = 0; i < testFiles.length; i++) {
//			String filename = testFiles[i].getAbsolutePath();
//			ArrayList <String> generatedList = search.resultListOfMfcc(filename, true, Distance.BHAT);
//			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
//			generatedResults.add(generatedResult);
//		}
////		String resultFileHeader = "###MFCC Test Result -- BHAT####\n";
//			
//		evaluation.evaluateTest(testFiles, generatedResults, resultFile, resultFileHeader, isAudio);
		
		String resultFile = "result.txt";
		EvaluationFacade evaluation = new EvaluationFacade();
		evaluation.writeToFile(resultFile, false, "");
		
		// MFCC 
		evaluateResult(testFiles, Distance.BHAT, null, search, isAudio, Feature.MFCC);
		evaluateResult(testFiles, Distance.CHEB, null, search, isAudio, Feature.MFCC);
		evaluateResult(testFiles, Distance.CITYBLOCK, null, search, isAudio, Feature.MFCC);
		evaluateResult(testFiles, Distance.COSINE, null, search, isAudio, Feature.MFCC);
		evaluateResult(testFiles, Distance.EUCLID, null, search, isAudio, Feature.MFCC);
		evaluateResult(testFiles, Distance.RBF, null, search, isAudio, Feature.MFCC);
		
		// Energy
		evaluateResult(testFiles, Distance.BHAT, null, search, isAudio, Feature.ENERGY);
		evaluateResult(testFiles, Distance.CHEB, null, search, isAudio, Feature.ENERGY);
		evaluateResult(testFiles, Distance.CITYBLOCK, null, search, isAudio, Feature.ENERGY);
		evaluateResult(testFiles, Distance.COSINE, null, search, isAudio, Feature.ENERGY);
		evaluateResult(testFiles, Distance.EUCLID, null, search, isAudio, Feature.ENERGY);
		evaluateResult(testFiles, Distance.RBF, null, search, isAudio, Feature.ENERGY);
		
		// MS
		evaluateResult(testFiles, Distance.BHAT, null, search, isAudio, Feature.MS);
		evaluateResult(testFiles, Distance.CHEB, null, search, isAudio, Feature.MS);
		evaluateResult(testFiles, Distance.CITYBLOCK, null, search, isAudio, Feature.MS);
		evaluateResult(testFiles, Distance.COSINE, null, search, isAudio, Feature.MS);
		evaluateResult(testFiles, Distance.EUCLID, null, search, isAudio, Feature.MS);
		evaluateResult(testFiles, Distance.RBF, null, search, isAudio, Feature.MS);
		
		//Zero-Crossing Rate
		evaluateResult(testFiles, Distance.BHAT, null, search, isAudio, Feature.ZCR);
		evaluateResult(testFiles, Distance.CHEB, null, search, isAudio, Feature.ZCR);
		evaluateResult(testFiles, Distance.CITYBLOCK, null, search, isAudio, Feature.ZCR);
		evaluateResult(testFiles, Distance.COSINE, null, search, isAudio, Feature.ZCR);
		evaluateResult(testFiles, Distance.EUCLID, null, search, isAudio, Feature.ZCR);
		evaluateResult(testFiles, Distance.RBF, null, search, isAudio, Feature.ZCR);
		
		//2-Features Combinations
		evaluateResult(testFiles, Distance.CITYBLOCK, Distance.CITYBLOCK, search, isAudio, Feature.MFCCENERGY);
		evaluateResult(testFiles, Distance.CITYBLOCK, Distance.CITYBLOCK, search, isAudio, Feature.MFCCZCR);
		evaluateResult(testFiles, Distance.CITYBLOCK, Distance.BHAT, search, isAudio, Feature.MFCCMS);
		evaluateResult(testFiles, Distance.CITYBLOCK, Distance.BHAT, search, isAudio, Feature.ENERGYMS);
		evaluateResult(testFiles, Distance.CITYBLOCK, Distance.CITYBLOCK, search, isAudio, Feature.ENERGYZCR);
		evaluateResult(testFiles, Distance.BHAT, Distance.CITYBLOCK, search, isAudio, Feature.MSZCR);
		
		//3-Features Combinations
		evaluateResult(testFiles, null, null, search, isAudio, Feature.MFCC_ENERGY_MS);
		evaluateResult(testFiles, null, null, search, isAudio, Feature.MFCC_ENERGY_ZCR);
		evaluateResult(testFiles, null, null, search, isAudio, Feature.MFCC_MS_ZCR);
		evaluateResult(testFiles, null, null, search, isAudio, Feature.ENERGY_MS_ZCR);
		
		//4-Features Combinations
		evaluateResult(testFiles, null, null, search, isAudio, Feature.MFCC_ENERGY_MS_ZCR);
		*/
	}
	
	@SuppressWarnings("unused")
	private static void evaluateResult(File[] testFiles, Distance d1, Distance d2, SearchDemo search, boolean isAudio, Feature feature) {
		ArrayList<String[]>generatedResults = new ArrayList<String[]>();
		ArrayList <String> generatedList = new ArrayList<String>();
		for (int i = 0; i < testFiles.length; i++) {
			String filename = testFiles[i].getAbsolutePath();
			switch (feature) {
				case MFCC:
					generatedList = search.resultListOfMfcc(filename, isAudio, d1);
					break;
				case ENERGY:
					generatedList = search.resultListOfEnergy(filename, isAudio, d1);
					break;
				case MS:
					generatedList = search.resultListOfSpectrum(filename, isAudio, d1);
					break;
				case ZCR:
					generatedList = search.resultListOfZeroCrossing(filename, isAudio, d1);
					break;
				case MFCCENERGY:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.MFCC, Feature.ENERGY);
					break;
				case MFCCZCR:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.MFCC, Feature.ZCR);
					break;
				case MFCCMS:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.MFCC, Feature.MS);
					break;
				case ENERGYMS:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.ENERGY, Feature.MS);
					break;
				case ENERGYZCR:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.ENERGY, Feature.ZCR);
					break;
				case MSZCR:
					generatedList = search.resultListOfTwoFeatures(filename, isAudio, d1, d2, Feature.MS, Feature.ZCR);
					break;
				case MFCC_ENERGY_MS:
					generatedList = search.resultListOfThreeFeatures(filename, isAudio, Feature.MFCC, Feature.ENERGY, Feature.MS);
					break;
				case MFCC_ENERGY_ZCR:
					generatedList = search.resultListOfThreeFeatures(filename, isAudio, Feature.MFCC, Feature.ENERGY, Feature.ZCR);
					break;
				case MFCC_MS_ZCR:
					generatedList = search.resultListOfThreeFeatures(filename, isAudio, Feature.MFCC, Feature.MS, Feature.ZCR);
					break;
				case ENERGY_MS_ZCR: 
					generatedList = search.resultListOfThreeFeatures(filename, isAudio, Feature.ENERGY, Feature.MS, Feature.ZCR);
					break;
				case MFCC_ENERGY_MS_ZCR:
					generatedList = search.resultListOfAllFeatures(filename, isAudio, Feature.ENERGY, Feature.MFCC, Feature.MS, Feature.ZCR);
					break;
				default:
					break;
			}
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		String resultFile = "result.txt";
		String resultFileHeader = "###" + feature + " Test Result -- " + d1 + " and " + d2 + "####\n";
		EvaluationFacade evaluation = new EvaluationFacade();
		evaluation.evaluateTest(testFiles, generatedResults, resultFile, resultFileHeader, isAudio);
	}
}
