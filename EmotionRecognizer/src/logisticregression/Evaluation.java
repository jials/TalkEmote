package logisticregression;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class Evaluation {
	public Vector <String> retrieveDataFromFile(String fileName) {
		Vector <String>  data = new Vector<String>();
		try {
			File file = new File(fileName);
		    BufferedReader br = new BufferedReader(new FileReader(file));
		    for(String line; (line = br.readLine()) != null; ) {
		        line = line.replaceAll("\\s", " ");
		        data.add(line);
		    }
		    br.close();
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return data;
	}
	
	public double calculateAccuracy(Vector <String> expected, Vector<String> actual) {
		if (expected.size() != actual.size()) {
			System.out.println("the dimension is different!");
			System.exit(-1);
		}
		
		int hit = 0;
		for (int i = 0; i < expected.size(); i++) {
			if (actual.get(i).equals(expected.get(i))) {
				hit++;
			}
		}
		double accuracy = (double) hit / (double) expected.size();
		return accuracy;
	}
	
	public static void main (String[] args) {
		String format = "format: java Evaluation expectedFile actualFile";
		if (args.length < 2) {
			System.out.println("too less arguments");
			System.out.println(format);
			System.exit(-1);
		} else if (args.length > 2) {
			System.out.println("too many arguments");
			System.out.println(format);
			System.exit(-1);
		}
		
		String expectedFile = args[0];
		String actualFile = args[1];
		
		Evaluation evaluation = new Evaluation();
		Vector<String> expectedVector = evaluation.retrieveDataFromFile(expectedFile);
		Vector<String> actualVector = evaluation.retrieveDataFromFile(actualFile);
		
		double accuracy = evaluation.calculateAccuracy(expectedVector, actualVector);
		System.out.println(accuracy * 100 + "%");
	}
	
} 