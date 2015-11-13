package Evaluation;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;



import Search.SearchDemo;
import Search.SearchDemo.Distance;
import Tool.Stats;

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
		
<<<<<<< HEAD
		String resultFile = "result.txt";
=======
		for (int i = 0; i < testFiles.length; i++) {
			String filename = testFiles[i].getAbsolutePath();
			ArrayList <String> generatedList = search.resultListOfMfcc(filename, true, SearchDemo.Distance.COSINE);
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		String resultFile = "result.txt";
		String resultFileHeader = "###MFCC Test Result####\n";
		boolean isAudio = true;
			
>>>>>>> master
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
<<<<<<< HEAD
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
=======
		generatedResults = new ArrayList<String[]>();

		for (int i = 0; i < testFiles.length; i++) {
			String filename = testFiles[i].getAbsolutePath();
			ArrayList <String> generatedList = search.resultListOfEnergy(filename, true, SearchDemo.Distance.COSINE);
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		resultFileHeader = "###Energy Test Result####\n";

		evaluation.evaluateTest(testFiles, generatedResults, resultFile, resultFileHeader, isAudio);
		
		// Magnitude Spectrum
		generatedResults = new ArrayList<String[]>();

		for (int i = 0; i < testFiles.length; i++) {
			String filename = testFiles[i].getAbsolutePath();
			ArrayList <String> generatedList = search.resultListOfSpectrum(filename, true, SearchDemo.Distance.COSINE);
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		resultFileHeader = "###Magnitude Spectrum Test Result####\n";

		evaluation.evaluateTest(testFiles, generatedResults, resultFile, resultFileHeader, isAudio);
>>>>>>> master
		
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
	}
	
	private static void evaluateResult(File[] testFiles, Distance d1, Distance d2, SearchDemo search, boolean isAudio, Feature feature) {
		ArrayList<String[]>generatedResults = new ArrayList<String[]>();
		ArrayList <String> generatedList = new ArrayList<String>();
		for (int i = 0; i < testFiles.length; i++) {
			String filename = testFiles[i].getAbsolutePath();
<<<<<<< HEAD
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
			}
=======
			ArrayList <String> generatedList = search.resultListOfZeroCrossing(filename, true, SearchDemo.Distance.COSINE);
>>>>>>> master
			String[] generatedResult = generatedList.toArray(new String[generatedList.size()]);
			generatedResults.add(generatedResult);
		}
		String resultFile = "result.txt";
		String resultFileHeader = "###" + feature + " Test Result -- " + d1 + " and " + d2 + "####\n";
		EvaluationFacade evaluation = new EvaluationFacade();
		evaluation.evaluateTest(testFiles, generatedResults, resultFile, resultFileHeader, isAudio);
	}
}
