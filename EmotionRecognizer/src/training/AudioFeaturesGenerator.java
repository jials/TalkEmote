package training;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Vector;

import signal.WavObj;
import signal.WaveIO;
import evaluation.EvaluationFacade;
import feature.Energy;
import feature.MFCC;
import feature.MagnitudeSpectrum;
import feature.ZeroCrossing;

public class AudioFeaturesGenerator {
	public static final String EXT_TXT = ".txt";
	public static final String EXT_WAV = ".wav";
	private static final String FILEPATH_EMOTION_TRAIN = "data/input/EmotionSpeechDatabase_Toronto";
	public static final String FILEPATH_FEATURE_OUT = "data/feature";
	public static final String EMOTION_ZERO_CROSSING = FILEPATH_FEATURE_OUT + "/emotion_zerocrossing.txt";
	public static final String EMOTION_SPECTRUM = FILEPATH_FEATURE_OUT + "/emotion_spectrum.txt";
	public static final String EMOTION_ENERGY = FILEPATH_FEATURE_OUT + "/emotion_energy.txt";
	public static final String EMOTION_MFCC = FILEPATH_FEATURE_OUT + "/emotion_mfcc.txt";
	
	public static final String FILEPATH_AUDIO_IEMOCAP_SEGMENT = "data/input/IEMOCAP_segment/";
	public static final String FILEPATH_AUDIO_IEMOCAP_TRAIN = "data/input/IEMOCAP_database";
	public static final String FILEPATH_AUDIO_IEMOCAP_LABEL = "data/input/IEMOCAP_label/";
	public static final String FILEPATH_AUDIO_IEMOCAP_SEGMENT_TRANS = "data/input/IEMOCAP_segment_transcriptions/";


	public static final String EMOTION_IEMOCAP_MFCC = FILEPATH_FEATURE_OUT + "/emotion_iemocap_mfcc.txt";

	private Vector <String> retrieveEmotionFromWavFile(String wavFile) {
		if (!wavFile.endsWith(EXT_WAV)) {
			return null;
		}
		String labelFile = wavFile.replace(EXT_WAV, EXT_TXT);
		String labelPath = FILEPATH_AUDIO_IEMOCAP_LABEL + labelFile;
		
		return readIemocapLabelFile(labelPath);
	}
	
	@SuppressWarnings("resource")
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
	
	public boolean computeIemocapLabelMFCC(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		
		HashMap <String, String> emotions = EvaluationFacade.getIemocapEmotionHashMap();	
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			String audioFileName = audioFiles[i].getName();
			WaveIO waveio = new WaveIO();
			WavObj obj = waveio.constructWavObj(audioName);
			
			Vector <String> lines = retrieveEmotionFromWavFile(audioFileName);
			
			
			for (int j = 0; j < lines.size(); j++) {
				System.out.println(j);
				
				String line = lines.get(j);
				line = line.substring(1).replaceAll("\\s+", " ");
				String[] tokens = line.split("]");
				String time = tokens[0].trim();
				String[] timeTokens = time.split("-");
				double startTime = Double.parseDouble(timeTokens[0].trim());
				double endTime = Double.parseDouble(timeTokens[1].trim());
				
				String nameAndEmotion = tokens[1].trim();
				String[] nameAndEmotionTokens = nameAndEmotion.split(" ");
				String name = nameAndEmotionTokens[0].trim();
				String emotion = nameAndEmotionTokens[1].trim();
								
				short[] signal = obj.getSignalWithoutFirstFewSeconds(startTime);
				signal = obj.getSignalWithFirstFewSeconds(endTime - startTime, signal);
				signal = obj.trimSilence(signal);
				
				if (obj.isShort(signal)) {
					continue;
				}
				
				String displayName = name + "_" + emotions.get(emotion) + AudioFeaturesGenerator.EXT_WAV;
				waveio.writeWave(signal, FILEPATH_AUDIO_IEMOCAP_SEGMENT + displayName);

				
				if (emotion.equals("xxx")) {
					continue;
				} 
				
				MFCC mfcc = new MFCC();
				mfcc.process(signal);
				double[] mean = mfcc.getMeanFeature();
				StringBuffer buffer = new StringBuffer();
			
				buffer.append(displayName);

				
				for (int k = 0; k < mean.length; k++) {
					buffer.append(" "); 
					buffer.append(mean[k]);
				}
				buffer.append("\n");
				if (!writeToFile(filename, true, buffer.toString())) {
					return false;
				}
			}
		}
		return true;
	}
	
	/**
	 * @deprecated
	 * @param audioFiles
	 * @param filename
	 * @return
	 */
	@Deprecated
	public boolean computeIempcapMFCC(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			WaveIO waveio = new WaveIO();
			WavObj obj = waveio.constructWavObj(audioName);
			obj.removeSignalsWithinSeconds(2);
			double turn = 4.3;
			Vector <short[]> signals = obj.splitToSignals(turn);
			
			String audioFileName = audioFiles[i].getName();
			
			GrundtruthFileGenerator generator = GrundtruthFileGenerator.getObject();
			String avgGrundtruthFilename = generator.getAvgGrundtruthFilename(audioFileName);
			Vector <String> emotions = generator.readEmotion(avgGrundtruthFilename);
			
			boolean isEnded = false;
			while (emotions.size() != signals.size()) {
				turn += 0.1;
				signals = obj.splitToSignals(turn);

				if (turn - 4.6 > 0) {
					System.out.println(i);
					System.out.println(audioFileName);
					System.out.println(emotions.size());
					System.out.println(signals.size());
					isEnded = true;
					break;
				}
			}
			
			if (isEnded) {
				continue;
			}
			
			for (int j = 0; j < signals.size(); j++) {
				short[] signal = signals.get(j);
				MFCC mfcc = new MFCC();
				mfcc.process(signal);
				double[] mean = mfcc.getMeanFeature();
				StringBuffer buffer = new StringBuffer();
			
				buffer.append(audioFileName);
				buffer.append("__");
				buffer.append(j);
				buffer.append("_");
				buffer.append(emotions.get(j));
				
				for (int k = 0; k < mean.length; k++) {
					buffer.append(" "); 
					buffer.append(mean[k]);
				}
				buffer.append("\n");
				if (!writeToFile(filename, true, buffer.toString())) {
					return false;
				}
			}
		}
		return true;
	}
	
	public boolean computeMFCC(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			
			if (audioName.endsWith("null.wav")) {
				continue;
			}
			WaveIO waveio = new WaveIO();
			short[] signal = waveio.readWave(audioName);
			
			MFCC mfcc = new MFCC();
			mfcc.process(signal);
			double[] mean = mfcc.getMeanFeature();
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
	
	public boolean computeEnergy(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			WaveIO waveio = new WaveIO();
			short[] signal = waveio.readWave(audioName);
			
			Energy energy1 = new Energy();
			double[] feature = energy1.getFeature(signal);
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(audioFiles[i].getName());

			for (int j = 0; j < feature.length; j++) {
				buffer.append(" ");
				buffer.append(feature[j]);
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean computeMagnitudeSpectrum(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			WaveIO waveio = new WaveIO();
			short[] signal = waveio.readWave(audioName);
			
			MagnitudeSpectrum spectrum = new MagnitudeSpectrum();
			double[] feature = spectrum.getFeature(signal);
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(audioFiles[i].getName());
			for (int j = 0; j < feature.length; j++) {
				buffer.append(" ");
				buffer.append(feature[j]);
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		return true;
	}
	
	public boolean computeZeroCrossing(File[] audioFiles, String filename) {
		writeToFile(filename, false, "");
		for (int i = 0; i < audioFiles.length; i++) {
			String audioName = audioFiles[i].getAbsolutePath();
			WaveIO waveio = new WaveIO();
			short[] signal = waveio.readWave(audioName);
			
			ZeroCrossing zc = new ZeroCrossing();
			double[] feature = zc.getFeature(signal);
			StringBuffer buffer = new StringBuffer();
			
			buffer.append(audioFiles[i].getName());

			for (int j = 0; j < feature.length; j++) {
				buffer.append(" ");
				buffer.append(feature[j]);
			}
			buffer.append("\n");
			if (!writeToFile(filename, true, buffer.toString())) {
				return false;
			}
		}
		return true;
	}
	
	private boolean writeToFile(String filename, boolean isAppend, String line) {
		try (FileWriter fw = new FileWriter(filename, isAppend)){
			fw.write(line);
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		 
		return true;
	}
	
	public File createFile(String filepath) {
		File f = new File(filepath);
		f.getParentFile().mkdirs(); 
		try {
			f.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
		return f;
	}
	
	
	public static void main(String[] args) {
		AudioFeaturesGenerator featureGenerator = new AudioFeaturesGenerator();

		//trainEmotion(featureGenerator);
		trainIemocapEmotion(featureGenerator);
		//trainIemocapSegmentedEmotion(featureGenerator);
	}
	
	/**
	 * @param featureGenerator
	 */
	private static void trainIemocapEmotion(AudioFeaturesGenerator featureGenerator) {
		File emotionTrain = new File(KMeansClusteringTrainer.DIR_TRAINING_FILES);
		File[] emotionFiles = emotionTrain.listFiles();

		File emotionMfccFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_IEMOCAP_MFCC);
		

		if (!featureGenerator.computeIemocapLabelMFCC(emotionFiles, emotionMfccFile.getAbsolutePath())) {
			System.exit(-1);
		}
		
		
	}
	
	@SuppressWarnings("unused")
	private static void trainIemocapSegmentedEmotion(AudioFeaturesGenerator featureGenerator) {
		File emotionTrain = new File(FILEPATH_AUDIO_IEMOCAP_SEGMENT);
		File[] emotionFiles = emotionTrain.listFiles();

		File emotionMfccFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_IEMOCAP_MFCC);
		

		if (!featureGenerator.computeMFCC(emotionFiles, emotionMfccFile.getAbsolutePath())) {
			System.exit(-1);
		}
		
		
	}

	/**
	 * @param featureGenerator
	 */
	@SuppressWarnings("unused")
	private static void trainEmotion(AudioFeaturesGenerator featureGenerator) {
		File emotionTrain = new File(FILEPATH_EMOTION_TRAIN);
		File[] emotionFiles = emotionTrain.listFiles();

		File emotionMfccFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_MFCC);
		File emotionEnergyFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_ENERGY);
		File emotionSpectrumFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_SPECTRUM);
		File emotionZCFile = featureGenerator.createFile(AudioFeaturesGenerator.EMOTION_ZERO_CROSSING);

		if (!featureGenerator.computeMFCC(emotionFiles, emotionMfccFile.getAbsolutePath())) {
			System.exit(-1);
		} else if (!featureGenerator.computeEnergy(emotionFiles, emotionEnergyFile.getAbsolutePath())) {
			System.exit(-1);
		} else if (!featureGenerator.computeMagnitudeSpectrum(emotionFiles, emotionSpectrumFile.getAbsolutePath())) {
			System.exit(-1);
		} else if (!featureGenerator.computeZeroCrossing(emotionFiles, emotionZCFile.getAbsolutePath())) {
			System.exit(-1);
		}
	}
}
