package main;

import java.io.File;

import Search.SearchDemo;

public class EmotionRecognizerMain {
	private static final String DEFAULT_FILE = "test.wav";
	
	public static void main(String[] args) {
		SearchDemo search = new SearchDemo();
		File emotionFile = new File(DEFAULT_FILE);
		String emotion = search.classifyEmotion(emotionFile.getAbsolutePath());
		System.out.println(emotion);
	}
}
