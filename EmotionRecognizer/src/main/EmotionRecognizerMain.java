package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import search.SearchDemo;

public class EmotionRecognizerMain {
	private static final String DEFAULT_FILE = "test.wav";
	private static final String PYTHON_SPEECH_RECOGNITION_SENTIMENT = "wav_transcribe.py";
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
		
	public static void main(String[] args) {
		SearchDemo search = new SearchDemo();
		File emotionFile = new File(DEFAULT_FILE);
		String emotionString = getEmotionString(search, emotionFile);
		System.out.println(emotionString);
	}

	/**
	 * @param search
	 * @param emotionFile
	 */
	private static String getEmotionString(SearchDemo search, File emotionFile) {
		String emotion = search.classifyEmotion(emotionFile.getAbsolutePath());
		StringBuffer emotionBuffer = new StringBuffer();
				
		try {
			Process process = Runtime.getRuntime().exec("python " + PYTHON_SPEECH_RECOGNITION_SENTIMENT);
			InputStream is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String line;	

			int linenum = 0; 
			while ((line = br.readLine()) != null) {
				if (linenum == 0) {
					if (line.equals("Google Speech Recognition could not understand audio")) {
						
						emotionBuffer.append("message:|");
						emotionBuffer.append("emotion:" + emotion);
						return emotionBuffer.toString();
					} else {						
						char front = line.charAt(0);
						char frontCap = Character.toUpperCase(front);
						line = line.replaceFirst(front + "", frontCap + "");
						emotionBuffer.append("message:" + line  + "|");
					}
				} else if (linenum == 1) {
					line = line.trim();
					
					if (line.equals("pos") && emotion.equals("neutral")) {
						emotion = "happy";
					} else if (emotion.equals("other")) {
						emotion = "different";
					} else if (line.equals("neg") && emotion.equals("neutral")) {
						emotion = "frustration";
					}
					emotionBuffer.append("emotion:" + emotion);

					
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
		return emotionBuffer.toString();
	}

	/**
	 * @return
	 */
	public static HashMap<String, String> getEmotionMap() {
		HashMap <String, String> emotionMap = new HashMap <String, String>();
		emotionMap.put("excited", "pos");
		emotionMap.put("surprise", "pos");
		emotionMap.put("other", "other");
		emotionMap.put("fear", "neg");
		emotionMap.put("happy", "pos");
		emotionMap.put("sad", "neg");
		emotionMap.put("frustration", "neg");
		emotionMap.put("angry", "neg");
		emotionMap.put("neutral", "neutral");
		emotionMap.put("disgust", "neg");
		return emotionMap;
	}
}
