package search;

import java.io.IOException;

import training.SvmModelGenerator;
import evaluation.EvaluationFacade;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;

public class SvmEmotionClassifier {
	svm_model _model = null;

	private static SvmEmotionClassifier _classifier = null;
	
	private SvmEmotionClassifier() {
		initializeModel();
	}
	
	public static SvmEmotionClassifier getObject() {
		if (_classifier == null) {
			_classifier = new SvmEmotionClassifier();
		}
		return _classifier;
	}

	/**
	 * 
	 */
	private boolean initializeModel() {
		try {
			_model = svm.svm_load_model(SvmModelGenerator.SVM_MODEL_FILE);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	public String classifyEmotion(double[] query) {
		svm_node[] queryFeature = new svm_node[query.length];
		for (int i = 0; i < query.length; i++) {
			queryFeature[i] = new svm_node();
			queryFeature[i].index = i + 1;
			queryFeature[i].value = query[i];
		}
		
		double value = svm.svm_predict(_model, queryFeature);
		int label = (int) value;
		label--;
		
		//String[] tags = EvaluationFacade.EMOTION_IEMOCAP_TAGS;
		String[] tags = EvaluationFacade.EMOTION_TAGS;
		if (label < 0) {
			label = 0;
		} else if (label > tags.length - 1) {
			label = tags.length - 1;
		}
		return tags[label];
	}
}
