package search;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import logisticregression.SurroundingWordsGenerator;
import signal.WaveIO;
import tool.SortHashMapByValue;
import distance.Bhattacharyya;
import distance.Chebychev;
import distance.CityBlock;
import distance.Cosine;
import distance.Euclidean;
import distance.RBFKernel;
import evaluation.Feature;
import feature.Energy;
import feature.MFCC;
import feature.MagnitudeSpectrum;
import feature.ZeroCrossing;

/**
 * Created by workshop on 9/18/2015.
 */
public class SearchDemo {
	public enum Distance {
		BHAT, CHEB, CITYBLOCK, COSINE, EUCLID, RBF
	}

	// All the scores for respective features, derived from result
	private static final double SCORE_MFCC_CITYBLOCK = (0.6485436893203882 + 0.6116421521375079) * 100 / 2;
	private static final double SCORE_ENERGY_CITYBLOCK = (0.34174757281553414 + 0.23915478818008193) * 100 / 2;
	private static final double SCORE_MS_BHAT = (0.6422330097087378 + 0.6197571985281075) * 100 / 2;
	private static final double SCORE_ZCR_CITYBLOCK = (0.2082524271844662 + 0.08922754147898056) * 100 / 2;

	private SurroundingWordsGenerator _gen = null;

	
	/*
	 * private static final double SCORE_MFCC_BHAT = (0.15922330097087398 +
	 * 0.07504099469168307) * 100 / 2; private static final double
	 * SCORE_MFCC_CHEB = (0.566504854368932 + 0.523733426615182) * 100 / 2;
	 * private static final double SCORE_MFCC_COSINE = (0.6446601941747572 +
	 * 0.6060661329091586) * 100 / 2; private static final double
	 * SCORE_MFCC_EUCLID = (0.6388349514563108 + 0.5982077549822674) * 100 / 2;
	 * private static final double SCORE_MFCC_RBF = (0.2233009708737864 +
	 * 0.10244177489661272) * 100 / 2;
	 * 
	 * private static final double SCORE_ENERGY_BHAT = (0.18349514563106806 +
	 * 0.09300925241260088) * 100 / 2; private static final double
	 * SCORE_ENERGY_CHEB = (0.2383495145631068 + 0.12263912080710379) * 100 / 2;
	 * private static final double SCORE_ENERGY_COSINE = (0.1849514563106797 +
	 * 0.0941678792925825) * 100 / 2; private static final double
	 * SCORE_ENERGY_EUCLID = (0.3165048543689321 + 0.2054393308297469) * 100 /
	 * 2; private static final double SCORE_ENERGY_RBF = (0.024271844660194174 +
	 * 0.023580810965162763) * 100 / 2;
	 * 
	 * private static final double SCORE_MS_CHEB = (0.5990291262135923+
	 * 0.5506483078504517) * 100 / 2; private static final double
	 * SCORE_MS_CITYBLOCK = (0.5762135922330098 + 0.5393107971507719) * 100 / 2;
	 * private static final double SCORE_MS_COSINE = (0.6325242718446604 +
	 * 0.6099815630868377) * 100 / 2; private static final double
	 * SCORE_MS_EUCLID = (0.6082524271844659 + 0.5750841187302682) * 100 / 2;
	 * private static final double SCORE_MS_RBF = (0.11213592233009712 +
	 * 0.07202929132621565) * 100 / 2;
	 * 
	 * private static final double SCORE_ZCR_BHAT = (0.12524271844660195 +
	 * 0.03864605862141103) * 100 / 2; private static final double
	 * SCORE_ZCR_CHEB = (0.2082524271844662 + 0.08922754147898056) * 100 / 2;
	 * private static final double SCORE_ZCR_COSINE = (0.12524271844660195 +
	 * 0.03864605862141103) * 100 / 2; private static final double
	 * SCORE_ZCR_EUCLID = (0.2082524271844662 + 0.08922754147898056) * 100 / 2;
	 * private static final double SCORE_ZCR_RBF = (0.10388349514563112 +
	 * 0.05187985153170457) * 100 / 2;
	 */

	/**
	 * Please replace the 'trainPath' with the specific path of train set in
	 * your PC.
	 */

	private HashMap<String, double[]> _emotionEnergy = null;
	private HashMap<String, double[]> _emotionMfcc = null;
	private HashMap<String, double[]> _emotionSpectrum = null;
	private HashMap<String, double[]> _emotionZeroCrossing = null;

	public SearchDemo() {
		_gen = new SurroundingWordsGenerator();
		_gen.readSurroundingWordFile();
		_gen.readTranscripts();
	}

	/***
	 * Get the distances between features of the selected query audio and ones
	 * of the train set;
	 * 
	 * @param query
	 *            the selected query audio file; distance the type of distance
	 *            measurement;
	 * @return the top 20 similar audio files;
	 */
	public ArrayList<String> resultListOfEnergy(String query, @SuppressWarnings("unused") boolean isAudio,
			Distance distance) {
		if (_emotionEnergy == null) {
			_emotionEnergy = readFeature("data/feature/emotion_energy.txt");
		}

		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		Energy ms = new Energy();
		double[] msFeature1 = ms.getFeature(inputSignal);
		HashMap<String, Double> simList = new HashMap<String, Double>();

		/**
		 * Load the offline file of features (the result of function
		 * 'trainFeatureList()'), modify it by yourself please;
		 */
		HashMap<String, double[]> trainFeatureList = null;

		trainFeatureList = _emotionEnergy;

		// System.out.println(trainFeatureList.size() + "=====");
		switch (distance) {
		case BHAT:
			Bhattacharyya bhat = new Bhattacharyya();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						bhat.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CHEB:
			Chebychev cheb = new Chebychev();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cheb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CITYBLOCK:
			CityBlock cb = new CityBlock();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case COSINE:
			Cosine cos = new Cosine();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cos.getDistance(msFeature1, f.getValue()));
			}
			break;
		case EUCLID:
			Euclidean euc = Euclidean.getObject();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						euc.getDistance(msFeature1, f.getValue()));
			}
			break;
		case RBF:
			RBFKernel rbf = new RBFKernel();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						rbf.getDistance(msFeature1, f.getValue()));
			}
			break;
		default:
			break;
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(simList);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Get the distances between features of the selected query audio and ones
	 * of the train set;
	 * 
	 * @param query
	 *            the selected query audio file; distance the type of distance
	 *            measurement;
	 * @return the top 20 similar audio files;
	 */
	@SuppressWarnings("unused")
	public ArrayList<String> resultListOfMfcc(String query, boolean isAudio,
			Distance distance) {
		if (_emotionMfcc == null) {
			_emotionMfcc = readFeature("data/feature/emotion_mfcc.txt");
		}

		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		MFCC ms = new MFCC();
		ms.process(inputSignal);
		double[] msFeature1 = ms.getMeanFeature();
		HashMap<String, Double> simList = new HashMap<String, Double>();

		/**
		 * Load the offline file of features (the result of function
		 * 'trainFeatureList()'), modify it by yourself please;
		 */
		HashMap<String, double[]> trainFeatureList = null;

		trainFeatureList = _emotionMfcc;

		// System.out.println(trainFeatureList.size() + "=====");
		switch (distance) {
		case BHAT:
			Bhattacharyya bhat = new Bhattacharyya();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						bhat.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CHEB:
			Chebychev cheb = new Chebychev();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cheb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CITYBLOCK:
			CityBlock cb = new CityBlock();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case COSINE:
			Cosine cos = new Cosine();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cos.getDistance(msFeature1, f.getValue()));
			}
			break;
		case EUCLID:
			Euclidean euc = Euclidean.getObject();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						euc.getDistance(msFeature1, f.getValue()));
			}
			break;
		case RBF:
			RBFKernel rbf = new RBFKernel();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						rbf.getDistance(msFeature1, f.getValue()));
			}
			break;
		default:
			break;
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(simList);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Get the distances between features of the selected query audio and ones
	 * of the train set;
	 * 
	 * @param query
	 *            the selected query audio file; distance the type of distance
	 *            measurement;
	 * @return the top 20 similar audio files;
	 */
	public ArrayList<String> resultListOfZeroCrossing(String query,
			@SuppressWarnings("unused") boolean isAudio, Distance distance) {
		if (_emotionZeroCrossing == null) {
			_emotionZeroCrossing = readFeature("data/feature/emotion_zerocrossing.txt");
		}

		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		ZeroCrossing ms = new ZeroCrossing();
		double[] msFeature1 = ms.getFeature(inputSignal);
		HashMap<String, Double> simList = new HashMap<String, Double>();

		/**
		 * Load the offline file of features (the result of function
		 * 'trainFeatureList()'), modify it by yourself please;
		 */
		HashMap<String, double[]> trainFeatureList = null;

		trainFeatureList = _emotionZeroCrossing;

		// System.out.println(trainFeatureList.size() + "=====");
		switch (distance) {
		case BHAT:
			Bhattacharyya bhat = new Bhattacharyya();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						bhat.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CHEB:
			Chebychev cheb = new Chebychev();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cheb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CITYBLOCK:
			CityBlock cb = new CityBlock();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case COSINE:
			Cosine cos = new Cosine();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cos.getDistance(msFeature1, f.getValue()));
			}
			break;
		case EUCLID:
			Euclidean euc = Euclidean.getObject();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						euc.getDistance(msFeature1, f.getValue()));
			}
			break;
		case RBF:
			RBFKernel rbf = new RBFKernel();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						rbf.getDistance(msFeature1, f.getValue()));
			}
			break;
		default:
			break;
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(simList);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Get the distances between features of the selected query audio and ones
	 * of the train set;
	 * 
	 * @param query
	 *            the selected query audio file; distance, the type of distance
	 *            measurement;
	 * @return the top 20 similar audio files;
	 */
	@SuppressWarnings("unused")
	public ArrayList<String> resultListOfSpectrum(String query,
			boolean isAudio, Distance distance) {
		if (_emotionSpectrum == null) {
			_emotionSpectrum = readFeature("data/feature/emotion_spectrum.txt");
		}

		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		MagnitudeSpectrum ms = new MagnitudeSpectrum();
		double[] msFeature1 = ms.getFeature(inputSignal);
		HashMap<String, Double> simList = new HashMap<String, Double>();

		/**
		 * Load the offline file of features (the result of function
		 * 'trainFeatureList()'), modify it by yourself please;
		 */
		HashMap<String, double[]> trainFeatureList = null;

		trainFeatureList = _emotionSpectrum;

		// System.out.println(trainFeatureList.size() + "=====");
		switch (distance) {
		case BHAT:
			Bhattacharyya bhat = new Bhattacharyya();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						bhat.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CHEB:
			Chebychev cheb = new Chebychev();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cheb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case CITYBLOCK:
			CityBlock cb = new CityBlock();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cb.getDistance(msFeature1, f.getValue()));
			}
			break;
		case COSINE:
			Cosine cos = new Cosine();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						cos.getDistance(msFeature1, f.getValue()));
			}
			break;
		case EUCLID:
			Euclidean euc = Euclidean.getObject();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						euc.getDistance(msFeature1, f.getValue()));
			}
			break;
		case RBF:
			RBFKernel rbf = new RBFKernel();
			for (Map.Entry<String, double[]> f : trainFeatureList.entrySet()) {
				simList.put(f.getKey(),
						rbf.getDistance(msFeature1, f.getValue()));
			}
			break;
		default:
			break;
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(simList);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Obtain the result list of two different features
	 * 
	 * @param query
	 *            the selected query audio file; d1 the type of distance measure
	 *            for feature1 d2 the type of distance measure for feature2 f1
	 *            the type of first feature f2 the type of second feature
	 * @return the top 20 similar audio files;
	 */
	public ArrayList<String> resultListOfTwoFeatures(String query,
			boolean isAudio, Distance d1, Distance d2, Feature f1, Feature f2) {
		ArrayList<String> resultF1 = new ArrayList<String>();
		ArrayList<String> resultF2 = new ArrayList<String>();
		double weightF1 = 0.0;
		double weightF2 = 0.0;
		switch (f1) {
		case MFCC:
			resultF1 = resultListOfMfcc(query, isAudio, d1);
			// Hard coded distance weight, only use the best distance for each
			// feature
			weightF1 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF1 = resultListOfSpectrum(query, isAudio, d1);
			weightF1 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF1 = resultListOfEnergy(query, isAudio, d1);
			weightF1 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF1 = resultListOfZeroCrossing(query, isAudio, d1);
			weightF1 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f2) {
		case MFCC:
			resultF2 = resultListOfMfcc(query, isAudio, d2);
			weightF2 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF2 = resultListOfSpectrum(query, isAudio, d2);
			weightF2 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF2 = resultListOfEnergy(query, isAudio, d2);
			weightF2 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF2 = resultListOfZeroCrossing(query, isAudio, d2);
			weightF2 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		HashMap<String, Double> audioToScore = new HashMap<String, Double>();
		for (int i = 0; i < resultF1.size(); i++) {
			if (audioToScore.containsKey(resultF1.get(i))) {
				double originalValue = audioToScore.get(resultF1.get(i))
						.doubleValue();
				audioToScore.put(resultF1.get(i), originalValue + (20 - i)
						* weightF1);
			} else {
				audioToScore.put(resultF1.get(i), (20 - i) * weightF1);
			}
		}

		for (int i = 0; i < resultF2.size(); i++) {
			if (audioToScore.containsKey(resultF2.get(i))) {
				double originalValue = audioToScore.get(resultF2.get(i))
						.doubleValue();
				audioToScore.put(resultF2.get(i), originalValue + (20 - i)
						* weightF2);
			} else {
				audioToScore.put(resultF2.get(i), (20 - i) * weightF2);
			}
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(audioToScore);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Obtain the result list of three different features
	 * 
	 * @param query
	 *            the selected query audio file; f1 the type of first feature f2
	 *            the type of second feature f3 the type of third feature
	 * @return the top 20 similar audio files;
	 */
	public ArrayList<String> resultListOfThreeFeatures(String query,
			boolean isAudio, Feature f1, Feature f2, Feature f3) {
		ArrayList<String> resultF1 = new ArrayList<String>();
		ArrayList<String> resultF2 = new ArrayList<String>();
		ArrayList<String> resultF3 = new ArrayList<String>();
		double weightF1 = 0.0;
		double weightF2 = 0.0;
		double weightF3 = 0.0;

		switch (f1) {
		case MFCC:
			resultF1 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			// Hard coded distance weight, only use the best distance for each
			// feature
			weightF1 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF1 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF1 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF1 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF1 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF1 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF1 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f2) {
		case MFCC:
			resultF2 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			weightF2 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF2 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF2 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF2 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF2 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF2 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF2 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f3) {
		case MFCC:
			resultF3 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			weightF3 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF3 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF3 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF3 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF3 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF3 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF3 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		HashMap<String, Double> audioToScore = new HashMap<String, Double>();
		for (int i = 0; i < resultF1.size(); i++) {
			if (audioToScore.containsKey(resultF1.get(i))) {
				double originalValue = audioToScore.get(resultF1.get(i))
						.doubleValue();
				audioToScore.put(resultF1.get(i), originalValue + (20 - i)
						* weightF1);
			} else {
				audioToScore.put(resultF1.get(i), (20 - i) * weightF1);
			}
		}

		for (int i = 0; i < resultF2.size(); i++) {
			if (audioToScore.containsKey(resultF2.get(i))) {
				double originalValue = audioToScore.get(resultF2.get(i))
						.doubleValue();
				audioToScore.put(resultF2.get(i), originalValue + (20 - i)
						* weightF2);
			} else {
				audioToScore.put(resultF2.get(i), (20 - i) * weightF2);
			}
		}

		for (int i = 0; i < resultF3.size(); i++) {
			if (audioToScore.containsKey(resultF3.get(i))) {
				double originalValue = audioToScore.get(resultF3.get(i))
						.doubleValue();
				audioToScore.put(resultF3.get(i), originalValue + (20 - i)
						* weightF3);
			} else {
				audioToScore.put(resultF3.get(i), (20 - i) * weightF3);
			}
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(audioToScore);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/***
	 * Obtain the result list of four different features
	 * 
	 * @param query
	 *            the selected query audio file; f1 the type of first feature f2
	 *            the type of second feature f3 the type of third feature f4 the
	 *            type of fourth feature
	 * @return the top 20 similar audio files;
	 */
	public ArrayList<String> resultListOfAllFeatures(String query,
			boolean isAudio, Feature f1, Feature f2, Feature f3, Feature f4) {
		ArrayList<String> resultF1 = new ArrayList<String>();
		ArrayList<String> resultF2 = new ArrayList<String>();
		ArrayList<String> resultF3 = new ArrayList<String>();
		ArrayList<String> resultF4 = new ArrayList<String>();
		double weightF1 = 0.0;
		double weightF2 = 0.0;
		double weightF3 = 0.0;
		double weightF4 = 0.0;

		switch (f1) {
		case MFCC:
			resultF1 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			// Hard coded distance weight, only use the best distance for each
			// feature
			weightF1 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF1 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF1 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF1 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF1 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF1 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF1 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f2) {
		case MFCC:
			resultF2 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			weightF2 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF2 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF2 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF2 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF2 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF2 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF2 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f3) {
		case MFCC:
			resultF3 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			weightF3 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF3 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF3 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF3 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF3 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF3 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF3 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		switch (f4) {
		case MFCC:
			resultF4 = resultListOfMfcc(query, isAudio, Distance.CITYBLOCK);
			weightF4 = SCORE_MFCC_CITYBLOCK;
			break;
		case MS:
			resultF4 = resultListOfSpectrum(query, isAudio, Distance.BHAT);
			weightF4 = SCORE_MS_BHAT;
			break;
		case ENERGY:
			resultF4 = resultListOfEnergy(query, isAudio, Distance.CITYBLOCK);
			weightF4 = SCORE_ENERGY_CITYBLOCK;
			break;
		case ZCR:
			resultF4 = resultListOfZeroCrossing(query, isAudio,
					Distance.CITYBLOCK);
			weightF4 = SCORE_ZCR_CITYBLOCK;
			break;
		case ENERGYMS:
		case ENERGYZCR:
		case ENERGY_MS_ZCR:
		case MFCCENERGY:
		case MFCCMS:
		case MFCCZCR:
		case MFCC_ENERGY_MS:
		case MFCC_ENERGY_MS_ZCR:
		case MFCC_ENERGY_ZCR:
		case MFCC_MS_ZCR:
		case MSZCR:
		default:
			break;
		}

		HashMap<String, Double> audioToScore = new HashMap<String, Double>();
		for (int i = 0; i < resultF1.size(); i++) {
			if (audioToScore.containsKey(resultF1.get(i))) {
				double originalValue = audioToScore.get(resultF1.get(i))
						.doubleValue();
				audioToScore.put(resultF1.get(i), originalValue + (20 - i)
						* weightF1);
			} else {
				audioToScore.put(resultF1.get(i), (20 - i) * weightF1);
			}
		}

		for (int i = 0; i < resultF2.size(); i++) {
			if (audioToScore.containsKey(resultF2.get(i))) {
				double originalValue = audioToScore.get(resultF2.get(i))
						.doubleValue();
				audioToScore.put(resultF2.get(i), originalValue + (20 - i)
						* weightF2);
			} else {
				audioToScore.put(resultF2.get(i), (20 - i) * weightF2);
			}
		}

		for (int i = 0; i < resultF3.size(); i++) {
			if (audioToScore.containsKey(resultF3.get(i))) {
				double originalValue = audioToScore.get(resultF3.get(i))
						.doubleValue();
				audioToScore.put(resultF3.get(i), originalValue + (20 - i)
						* weightF3);
			} else {
				audioToScore.put(resultF3.get(i), (20 - i) * weightF3);
			}
		}

		for (int i = 0; i < resultF4.size(); i++) {
			if (audioToScore.containsKey(resultF4.get(i))) {
				double originalValue = audioToScore.get(resultF4.get(i))
						.doubleValue();
				audioToScore.put(resultF4.get(i), originalValue + (20 - i)
						* weightF4);
			} else {
				audioToScore.put(resultF4.get(i), (20 - i) * weightF4);
			}
		}

		SortHashMapByValue sortHM = new SortHashMapByValue(20);
		ArrayList<String> result = sortHM.sort(audioToScore);

		String out = query + ":";
		for (int j = 0; j < result.size(); j++) {
			out += "\t" + result.get(j);
		}

		System.out.println(out);
		return result;
	}

	/**
	 * Load the offline file of features (the result of function
	 * 'trainFeatureList()');
	 * 
	 * @param featurePath
	 *            the path of offline file including the features of training
	 *            set.
	 * @return the map of training features, Key is the name of file, Value is
	 *         the array/vector of features.
	 */
	@SuppressWarnings("resource")
	private HashMap<String, double[]> readFeature(String featurePath) {
		HashMap<String, double[]> fList = new HashMap<>();
		try {
			FileReader fr = new FileReader(featurePath);
			BufferedReader br = new BufferedReader(fr);

			String line = br.readLine();
			while (line != null) {
				line = line.replaceAll("\\s", " ");
				String[] split = line.trim().split(" ");
				if (split.length < 2)
					continue;
				double[] fs = new double[split.length - 1];
				for (int i = 1; i < split.length; i++) {
					fs[i - 1] = Double.valueOf(split[i]);
				}

				fList.put(split[0], fs);

				line = br.readLine();
			}
			br.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

		return fList;
	}

	/*
	 * These are the methds used to integrate with opensmile toolkit public
	 * String classifyEmotion(String query) { try {
	 * 
	 * 
	 * File file = new File(query); Process classification =
	 * Runtime.getRuntime().exec("\"opensmile\\SMILExtract_Release.exe\" -C " +
	 * "\"config\\IS09_emotion.conf\" -I " + "\"" + file.getAbsolutePath() +
	 * "\" -instname \"" + file.getName() + "\" " + "-O \"..\\test.arff\"",
	 * null, new File("opensmile/"));
	 * 
	 * InputStream is = classification.getInputStream(); InputStreamReader isr =
	 * new InputStreamReader(is); BufferedReader br = new BufferedReader(isr);
	 * String line;
	 * 
	 * while ((line = br.readLine()) != null) { System.out.println(line); }
	 * 
	 * 
	 * } catch (IOException e) { // TODO Auto-generated catch block
	 * e.printStackTrace(); } return null; }
	 * 
	 * public Vector<String> getEmotions() { WekaClassifier weka = new
	 * WekaClassifier(); Vector<String> featureLine =
	 * weka.getFeatures("test.arff");
	 * 
	 * Vector <String> emotions = new Vector<String>();
	 * 
	 * for (int i = 0; i < featureLine.size(); i++) { String[] featureTokens =
	 * featureLine.get(i).split(" "); double[] features = new
	 * double[featureTokens.length - 1]; for (int j = 0; j < features.length;
	 * j++) { features[i] = Double.parseDouble(featureTokens[i + 1]); }
	 * SvmEmotionClassifier classifier =
	 * search.SvmEmotionClassifier.getObject(); String emotion =
	 * classifier.classifyEmotion(features); emotions.add(emotion); } return
	 * emotions; }
	 */

	// these are previous methods when we are using our own MFCC feature to
	// calculate

	public String classifyEmotion(String query) {
		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		return classifyEmotion(inputSignal);
	}
	
	public String classifyEmotion(String query, String line) {
		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		return classifyEmotion(inputSignal, line);
	}
	
	public String classifyEmotion(String query, File file) {
		WaveIO waveIO = new WaveIO();

		short[] inputSignal = waveIO.readWave(query);
		return classifyEmotion(inputSignal, file);
	}

	/**
	 * @param inputSignal
	 * @return
	 */

	public String classifyEmotion(short[] inputSignal) {
		MFCC ms = new MFCC();
		ms.process(inputSignal);
		double[] msFeature1 = ms.getMeanFeature();
		SvmEmotionClassifier classifier = search.SvmEmotionClassifier
				.getObject();
		String emotion = classifier.classifyEmotion(msFeature1);
		return emotion;
	}
	
	public String classifyEmotion(short[] inputSignal, File file) {
		MFCC ms = new MFCC();
		ms.process(inputSignal);
		double[] msFeature1 = ms.getMeanFeature();
		double[] ling = _gen.getFeatureVector(file);
		double[] features = fuseFeatures(msFeature1, ling);
		SvmEmotionClassifier classifier = search.SvmEmotionClassifier
				.getObject();
		String emotion = classifier.classifyEmotion(features);
		return emotion;
	}
	
	public String classifyEmotion(short[] inputSignal, String line) {
		MFCC ms = new MFCC();
		ms.process(inputSignal);
		double[] msFeature1 = ms.getMeanFeature();
		double[] ling = _gen.getFeatureVector(line);
		double[] features = fuseFeatures(msFeature1, ling);
		SvmEmotionClassifier classifier = search.SvmEmotionClassifier
				.getObject();
		String emotion = classifier.classifyEmotion(features);
		return emotion;
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

}
