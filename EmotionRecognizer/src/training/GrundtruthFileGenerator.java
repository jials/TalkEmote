package training;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TreeMap;
import java.util.Vector;

import search.KMeansClusteringClassifier;

public class GrundtruthFileGenerator {
	private static GrundtruthFileGenerator _generator = null;
	
	private GrundtruthFileGenerator() {
		
	}
	
	public static GrundtruthFileGenerator getObject() {
		if (_generator == null) {
			_generator = new GrundtruthFileGenerator();
		}
		return _generator;
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
	
	public void generateSingleGrundtruthFile(TreeMap <String, Integer> emotionIndex) {
		File testingDir = new File(KMeansClusteringClassifier.DIR_TESTING_FILES);
		File[] testingFiles = testingDir.listFiles();
		
		File trainingDir = new File(KMeansClusteringTrainer.DIR_TRAINING_FILES);
		File[] trainingFiles = trainingDir.listFiles();
		
		generateSingleGrundtruthFile(testingFiles, emotionIndex);
		generateSingleGrundtruthFile(trainingFiles, emotionIndex);
	}
    
	public void generateSingleGrundtruthFile(File[] wavFiles, 
											 TreeMap <String, Integer> emotionIndex) {
		
		for (int i = 0; i < wavFiles.length; i++) {
			String name = wavFiles[i].getName();
			Vector < Vector <String> > grundtruthEmotions = new Vector < Vector <String> >();
			for (int j = 0; j < KMeansClusteringClassifier.EVALUATORS.length; j++) {
				String grundtruthName = getGrundtruthName(name, j);
				File file = new File(grundtruthName);
				if (file.exists()) {
					Vector <String> emotion = readEmotion(grundtruthName);
					grundtruthEmotions.add(emotion);
				}
			}
			String filename = getAvgGrundtruthFilename(name);
			writeToFile(filename, false, "");


			for (int k = 0; k < grundtruthEmotions.get(0).size(); k++) {
				int[] temp = new int[KMeansClusteringClassifier.EMOTIONS.length + 1];
				for (int j = 0; j < temp.length; j++) {
					temp[j] = 0;
				}
				for (int j = 0; j < grundtruthEmotions.size(); j++) {
					Vector <String> emotions = grundtruthEmotions.get(j);
					String emotion = emotions.get(k);
					int emotionIdx = KMeansClusteringClassifier.INDEX_EMOTION_NOT_EXIST;
					if (emotionIndex.containsKey(emotion)) {
						emotionIdx = emotionIndex.get(emotion);
					}
					temp[emotionIdx]++;
				}
				int max = temp[0];
				int chosenIndex = 0;
				for (int j = 1; j < temp.length; j++) {
					if (max < temp[j]) {
						max = temp[j];
						chosenIndex = j;
					}
				}
				writeToFile(filename, true, filename + "_" + k +
						":" + KMeansClusteringClassifier.EMOTIONS[chosenIndex] + ";\n");
			} 
			
		}
		
		
	}

	/**
	 * @param name
	 * @return
	 */
	public String getAvgGrundtruthFilename(String name) {
		String filename = KMeansClusteringClassifier.DIR_GRUNDTRUTH + 
				name.replace(KMeansClusteringClassifier.EXT_WAV, 
						KMeansClusteringClassifier.AVG_GRUNDTRUTH);
		return filename;
	}

	/**
	 * @param name
	 * @param j
	 * @return
	 */
	private String getGrundtruthName(String name, int j) {
		String grundtruthName = KMeansClusteringClassifier.DIR_GRUNDTRUTH + 
				name.replace(KMeansClusteringClassifier.EXT_WAV, 
						KMeansClusteringClassifier.EVALUATORS[j] + 
						KMeansClusteringClassifier.EXT_GRUNDTRUTH);
		return grundtruthName;
	}
	
	public Vector <String> readEmotion(String filename) {
		Vector <String> emotions = new Vector <String>();
		try{
            FileReader fr = new FileReader(filename);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
            	int beginIndex = line.indexOf(":");
            	int endIndex = line.indexOf(";");
            	String emotion = line.substring(beginIndex + 1, endIndex).trim();
            	emotions.add(emotion);
                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }		
		return emotions;
	}
	
	public static void main(String[] args) {
		TreeMap <String, Integer> emotionIndex = new TreeMap<String, Integer>();
    	for (int i = 0; i < KMeansClusteringClassifier.EMOTIONS.length; i++) {
    		emotionIndex.put(KMeansClusteringClassifier.EMOTIONS[i], i);
    	}
    	GrundtruthFileGenerator generator = GrundtruthFileGenerator.getObject();
    	generator.generateSingleGrundtruthFile(emotionIndex);
	}
}
