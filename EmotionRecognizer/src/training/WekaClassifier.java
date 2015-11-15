package training;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Vector;

import evaluation.EvaluationFacade;
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;

public class WekaClassifier {
	private final static int SCALE = 1;
	private final static int START = 0;
	
	public WekaClassifier() {
	}

	/**
	 * 
	 */
	public void processData(String datapath) {
		DataSource source;
		try {
			source = new DataSource(datapath);
			Instances train = source.getDataSet();
			// setting class attribute if the data format does not provide this
			// information
			// For example, the XRFF format saves the class attribute
			// information as well
			if (train.classIndex() == -1) {
				train.setClassIndex(EvaluationFacade.EMOTION_IEMOCAP_TAGS.length + 1);
			}

			Normalize norm = new Normalize();
			norm.setScale(SCALE);
			norm.setTranslation(START);
			norm.setInputFormat(train);
			Instances processed_train = Filter.useFilter(train, norm);

			saveInstancesToFile(processed_train);
			SvmModelGenerator svm = new SvmModelGenerator();
			svm.saveModelFile(SvmModelGenerator.SVM_MODEL_FILE);

			/*
			 * LibSVM svm = new LibSVM(); svm.setSVMType(new
			 * SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
			 * svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR,
			 * LibSVM.TAGS_KERNELTYPE)); svm.setCacheSize(500);
			 * svm.buildClassifier(norminalData);
			 * 
			 * Evaluation eval = new Evaluation(processed_train);
			 * eval.crossValidateModel(svm, processed_train, 10, new Random(1));
			 * System.out.println(eval.toSummaryString("\nResults\n======\n",
			 * false));
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public Vector <String> getFeatures(String datapath) {
		DataSource source;
		try {
			source = new DataSource(datapath);
			Instances train = source.getDataSet();
			// setting class attribute if the data format does not provide this
			// information
			// For example, the XRFF format saves the class attribute
			// information as well
			if (train.classIndex() == -1) {
				train.setClassIndex(EvaluationFacade.EMOTION_IEMOCAP_TAGS.length + 1);
			}

			Normalize norm = new Normalize();
			norm.setScale(SCALE);
			norm.setTranslation(START);
			norm.setInputFormat(train);
			Instances processed_train = Filter.useFilter(train, norm);

			return extractFeatureLines(processed_train);

			/*
			 * LibSVM svm = new LibSVM(); svm.setSVMType(new
			 * SelectedTag(LibSVM.SVMTYPE_C_SVC, LibSVM.TAGS_SVMTYPE));
			 * svm.setKernelType(new SelectedTag(LibSVM.KERNELTYPE_LINEAR,
			 * LibSVM.TAGS_KERNELTYPE)); svm.setCacheSize(500);
			 * svm.buildClassifier(norminalData);
			 * 
			 * Evaluation eval = new Evaluation(processed_train);
			 * eval.crossValidateModel(svm, processed_train, 10, new Random(1));
			 * System.out.println(eval.toSummaryString("\nResults\n======\n",
			 * false));
			 */
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public Vector<String> extractFeatureLines(Instances processed_train) {
		String[] lines = processed_train.toString().split("\n");
		Vector <String> featureLines = new Vector<String>();
		for (int i = 0; i < lines.length; i++) {
			String line = processInstanceLine(lines, i);
			if (line == null) {
				continue;
			}
			featureLines.add(line);
		}
		return featureLines;
	}

	private void saveInstancesToFile(Instances processed_train) {
		String filename = AudioFeaturesGenerator.EMOTION_IEMOCAP_MFCC;

		saveInstancesToFile(processed_train, filename);
	}

	public void saveInstancesToFile(Instances processed_train, String filename) {
		writeToFile(filename, false, "");
		String[] lines = processed_train.toString().split("\n");
		for (int i = 0; i < lines.length; i++) {
			String line = processInstanceLine(lines, i);
			if (line == null) {
				continue;
			}
			writeToFile(filename, true, line + "\n");
		}
	}

	/**
	 * @param lines
	 * @param i
	 * @return
	 */
	public String processInstanceLine(String[] lines, int i) {
		String line = lines[i];

		if (line.startsWith("@")) {
			return null;
		} else if (line.trim().isEmpty()) {
			return null;
		}
		line = line.replace(",", " ");
		line = line.replace("'", "");
		line = line.replace("?", "");
		line = line.trim();
		return line;
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

	public static void main(String[] args) {
		WekaClassifier weka = new WekaClassifier();
		weka.processData("out.arff");
	}
}
