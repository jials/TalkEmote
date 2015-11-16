package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;



public class SurroundingWordsGenerator {
	public static final String FILEPATH_AUDIO_IEMOCAP_TRANS = "data/input/IEMOCAP_transcriptions/";
	private HashMap <String, String> _transcripts = null;
	
	private static String[] STOP_WORDS = null;
	private static final String STOP_WORDS_FILE = "stopwd.txt";
	
	private TreeMap <String, Integer> _surroundingWords = null;
	
	public static final String FILE_SURROUNDING_WORDS = "surroundingwords.txt";
	public static final String FILE_TRANSCRIPTS = "transcripts.txt";
	
	public SurroundingWordsGenerator() {
		_transcripts = new HashMap <String, String>();
		_surroundingWords = new TreeMap <String, Integer>();
		
		readStopWordsFile(STOP_WORDS_FILE);

	}
	
	public double[] getFeatureVector(File file) {
		double[] featureVector = new double[_surroundingWords.size()];
		Arrays.fill(featureVector, 0);

		
		String name = file.getName();
		int lastIndex = name.lastIndexOf("_");
		name = name.substring(0, lastIndex);
		
		String line = _transcripts.get(name);
		if (line == null) {
			return featureVector;
		}
		
		line = line.replaceAll("[^a-zA-Z ]", " ").toLowerCase().trim();
		String[] words = line.split(" ");
		for (int i = 0; i < words.length; i++) {
			if (_surroundingWords.containsKey(words[i])) {
				int index = _surroundingWords.get(words[i]).intValue();
				featureVector[index] = 1;
			}
		}
		
		return featureVector;
	}
	
	public boolean readTranscripts() {
		try {
			File file = new File(FILE_TRANSCRIPTS);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim().toLowerCase();
		        if (line.isEmpty()) {
		        	continue;
		        }
		        String[] tokens = line.split(":==:");
		        String name = tokens[0].trim();
		        String sentence = tokens[1].trim();
		        _transcripts.put(name, sentence);
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public boolean readSurroundingWordFile() {
		try {
			File file = new File(FILE_SURROUNDING_WORDS);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim().toLowerCase();
		        if (line.isEmpty()) {
		        	continue;
		        }
		        String[] tokens = line.split(" ");
		        String word = tokens[0].trim();
		        int index = Integer.parseInt(tokens[1].trim());
		        _surroundingWords.put(word, index);
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	public void extractTranscriptsFromFiles(File[] files){
		
		
		readAllTranscripts(FILEPATH_AUDIO_IEMOCAP_TRANS, files);

		
		int index = 0;

		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			int lastIndex = name.lastIndexOf("_");
			name = name.substring(0, lastIndex);
			
			String line = _transcripts.get(name);
			if (line == null) {
				continue;
			}
			
			String[] tokens = line.split(" ");
			for (int j = 0; j < tokens.length; j++) {
				String word = tokens[j].trim();
				if (isStopWord(word)) {
					continue;
				} else if (word.isEmpty()) {
					continue;
				} else if (_surroundingWords.containsKey(word)) {
					continue;
				}
				_surroundingWords.put(word, index);
				index++;
			}
		}
		
		writeToFile(FILE_SURROUNDING_WORDS, false, "");

		for (Map.Entry<String, Integer> entry : _surroundingWords.entrySet()) {
		    String key = entry.getKey();
		    int value = entry.getValue().intValue();
			writeToFile(FILE_SURROUNDING_WORDS, true, key + " " + value + "\n");
		}
		saveAllTranscripts();

	}
	
	private void saveAllTranscripts() {
		writeToFile(FILE_TRANSCRIPTS, false, "");

		for (Map.Entry<String, String> entry : _transcripts.entrySet()) {
		    String key = entry.getKey();
		    String value = entry.getValue();
			writeToFile(FILE_TRANSCRIPTS, true, key + ":==:" + value + "\n");
		}
		
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
	
	
	private void readAllTranscripts(String transcriptsDir, File[] files) {
		HashSet <String> fileNames = new HashSet<String>();
		for (int i = 0; i < files.length; i++) {
			String name = files[i].getName();
			int lastIndex = name.lastIndexOf("_");
			name = name.substring(0, lastIndex);
			fileNames.add(name);
		}
		
		File dir = new File(transcriptsDir);
		File[] transcripts = dir.listFiles();
		for (int i = 0; i < transcripts.length; i++) {
			readTransFile(transcripts[i].getAbsolutePath(), fileNames);
		}
	}

	private boolean isStopWord(String word) {
		word = word.trim();
		for (String stopWord : STOP_WORDS) {
			if (stopWord.equals(word)) {
				return true;
			}	
		}
		return false;
	}
	
	private boolean readStopWordsFile(String trainFile) {
		try {
			File file = new File(trainFile);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    Vector <String> stopWords = new Vector<String>();
		    for(String line; (line = br.readLine()) != null; ) {
		        // process the line.
		        line = line.trim().toLowerCase();
		        stopWords.add(line);
		    }
		    br.close();

		    STOP_WORDS = new String[stopWords.size()];
		    for (int i = 0; i < stopWords.size(); i++) {
		    	STOP_WORDS[i] = stopWords.get(i);
		    }
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}
	
	private boolean readTransFile(String filename, HashSet <String> targetFileNames) {
		try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
            	String[] tokens = line.split(":");
            	String name = tokens[0].split(" ")[0].trim();
            	

            	if (!targetFileNames.contains(name)) {
                    line = br.readLine();
            		continue;
            	}            	

            	
            	String msg = tokens[1].replaceAll("[^a-zA-Z ]", " ").toLowerCase().trim();
            	System.out.println(name + " " + msg);
            	_transcripts.put(name, msg);
                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }		
		return true;
	}
	
	public static void main(String[] args) {
		File dir = new File(LogisticFuse.FILEPATH_IEMOCAP_SEGMENT);
		
		SurroundingWordsGenerator gen = new SurroundingWordsGenerator();
		gen.extractTranscriptsFromFiles(dir.listFiles());
	}
}
