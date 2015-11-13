package Training;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import Evaluation.EvaluationFacade;
import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class SvmModelGenerator {
	public static class IntegerSvmNode {
		int label;
		svm_node[] actualData;
	}
	
	public static class sortIntegerSvmNode implements Comparator <IntegerSvmNode> {

		@Override
		public int compare(IntegerSvmNode o1, IntegerSvmNode o2) {
			return o1.label - o2.label;
		}
		
	}
	
	private static final String EXT = ".wav";
	public static final String SVM_MODEL_FILE = AudioFeaturesGenerator.FILEPATH_FEATURE_OUT + "/svm_model.txt";
		
	svm_model _model = null;
	public SvmModelGenerator() {
		_model = trainModel();
	}
	
	public boolean saveModelFile(String filename) {
		try {
			svm.svm_save_model(filename, _model);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	
	private svm_problem retrieveSvmProblemFromFiles() {
		svm_problem prob = new svm_problem();
		HashMap <String, double[]> emotionMfcc = readFeature(AudioFeaturesGenerator.EMOTION_IEMOCAP_MFCC);
		
		int size = emotionMfcc.size();
		
		double[] outcomes = new double[size];
		svm_node[][] datas = null;
		int index = 0;
		
		IntegerSvmNode[] nodes = new IntegerSvmNode[size];
		for (Map.Entry<String, double[]> entry : emotionMfcc.entrySet()) {
		    String key = entry.getKey();
		    
		    nodes[index] = new IntegerSvmNode();
		    nodes[index].label = retrieveOutcome(key);
		    
		    double[] value = entry.getValue();
		    if (datas == null) {
		    	datas = new svm_node[size][value.length];
		    }
		    
		    nodes[index].actualData = new svm_node[value.length];
		    
		    for (int i = 0; i < value.length; i++) {
		    	nodes[index].actualData[i] = new svm_node();
		    	nodes[index].actualData[i].index = i + 1;
		    	nodes[index].actualData[i].value = value[i];
		    }
		    index++;
		}
		
		Arrays.sort(nodes, new sortIntegerSvmNode());
		
		for (int i = 0; i < nodes.length; i++) {
			outcomes[i] = nodes[i].label;
			datas[i] = nodes[i].actualData;
		}
		prob.l = size;
		prob.x = datas;
		prob.y = outcomes;
		
		return prob;
	}
	
	private svm_parameter getTrainingParameter() {
		svm_parameter param = new svm_parameter();
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.gamma = 1;
		param.eps = 0.001;
		param.cache_size = 100;
		param.C = 1;
		param.nu = 0.5;
		param.shrinking = 1;
		param.probability = 0;
		
		return param;	
	}
	
	private svm_model trainModel() {
		svm_problem prob = retrieveSvmProblemFromFiles();
		svm_parameter param = getTrainingParameter();
		svm_model model = svm.svm_train(prob, param);
		return model;
		
	}
	
	private int retrieveOutcome(String key) {
		String[] tags = EvaluationFacade.EMOTION_IEMOCAP_TAGS;
		for (int i = 0; i < tags.length; i++) {
			if (key.endsWith(tags[i] + EXT)) {
				System.out.print(key + " ");
				System.out.println(i);
				return i + 1;
			}
		}
		return 0;
	}
	
	private HashMap<String, double[]> readFeature(String featurePath){
        HashMap<String, double[]> fList = new HashMap<>();
        try{
            FileReader fr = new FileReader(featurePath);
            BufferedReader br = new BufferedReader(fr);

            String line = br.readLine();
            while(line != null){
            	line = line.replaceAll("\\s", " ");
                String[] split = line.trim().split(" ");
                if (split.length < 2)
                    continue;
                double[] fs = new double[split.length - 1];
                for (int i = 1; i < split.length; i ++){
                    fs[i-1] = Double.valueOf(split[i]);
                }

                fList.put(split[0], fs);

                line = br.readLine();
            }
            br.close();
        }catch (Exception e){
            e.printStackTrace();
        }

        return fList;
    }
	
	public static void main (String[] args) {
		SvmModelGenerator svm = new SvmModelGenerator();
		svm.saveModelFile(SVM_MODEL_FILE);
	}
}
