package evaluation;

//audiobook

import java.io.File;
import java.util.HashMap;
import java.util.Vector;

import search.SearchDemo;

public class EvaluationFacade {

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

	public EvaluationFacade() {

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
	
	/*
	private Vector <String> retrieveEmotionFromWavFile(String wavFile) {
		if (!wavFile.endsWith(AudioFeaturesGenerator.EXT_WAV)) {
			return null;
		}
		String labelFile = wavFile.replace(AudioFeaturesGenerator.EXT_WAV, AudioFeaturesGenerator.EXT_TXT);
		String labelPath = AudioFeaturesGenerator.FILEPATH_AUDIO_IEMOCAP_LABEL + labelFile;
		
		return readIemocapLabelFile(labelPath);
	}
	*/
	
	/*
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
	*/
	/*
	 * previous method, when we are not using opensmile library
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
	*/

	public void evaluateEmotionClassification(File[] testFiles, SearchDemo search) {
		File test = new File("test.arff");
		if (test.exists()) {
			if (!test.delete()) {
				System.out.println("file not deleted");
			}
		}
		
		int total = testFiles.length;
		int correct = 0;
		Vector <String> emotions = new Vector<String>();
		for (int i = 0; i < testFiles.length; i++) {
			String testFile = testFiles[i].getAbsolutePath();
			search.classifyEmotion(testFile);
			emotions = search.getEmotions();
			String fileName = testFiles[i].getName();

			String emotion = emotions.get(i);
			System.out.println(fileName + " " + emotion);
			if (emotion.equals("pleasant surprise")) {
				emotion = "ps";
			}
			
			if (testFile.contains(emotion)) {
				correct++;
			}
			
			/*
			String emotion = search.classifyEmotion(testFile);
			
			System.out.println(testFiles[i].getName() + " " + emotion);
			if (emotion.equals("pleasant surprise")) {
				emotion = "ps";
			}
			
			if (testFile.contains(emotion)) {
				correct++;
			}
			*/
		}
		
		System.out.println(emotions.size() + " " + testFiles.length);
		
		double accuracy = (double) correct / (double) total;
		System.out.println("result: " + accuracy * 100 + "%");
	}
	
	
	public static void main (String[] args) {
		//File audioTestDir = new File(FILEPATH_AUDIO_TEST);
		
		SearchDemo search = new SearchDemo();
		EvaluationFacade evaluation = new EvaluationFacade();
		File emotionTestDir = new File(FILEPATH_EMOTION_TEST);
		File[] emotionTestFiles = emotionTestDir.listFiles();
		//evaluation.evaluateIemocapEmotionClassification(emotionTestFiles, search);
		evaluation.evaluateEmotionClassification(emotionTestFiles, search);
		
	}
	
	
}
