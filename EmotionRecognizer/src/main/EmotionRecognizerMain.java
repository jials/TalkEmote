package main;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;

import Search.SearchDemo;

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
		//System.out.println(emotion);
		StringBuffer emotionBuffer = new StringBuffer();
		
		HashMap<String, String> emotionMap = getEmotionMap();
		
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
						emotionBuffer.append("feeling ");
						emotionBuffer.append(emotion);
						return emotionBuffer.toString();
					} else {
						emotionBuffer.append(line);
					}
				} else if (linenum == 1) {
					emotionBuffer.append(" --- feeling ");
					line = line.trim();
					
					if (emotionMap.get(emotion).equals(line)) {
						emotionBuffer.append(emotion);
					} else {
						emotionBuffer.append("different");
					}
					
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