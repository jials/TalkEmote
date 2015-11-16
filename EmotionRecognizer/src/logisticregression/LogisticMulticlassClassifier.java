package logisticregression;

import java.util.Collections;
import java.util.Vector;

public class LogisticMulticlassClassifier {
	private String[] _tags = null;
	private Sctest[] _classifiers = null;
	
	public LogisticMulticlassClassifier(String[] tags) {
		_tags = tags;
		_classifiers = new Sctest[_tags.length];
		for (int i = 0; i < _tags.length; i++) {
			_classifiers[i] = new Sctest(_tags[i], _tags[i] + LogisticMulticlassTrain.EXT_MODEL);
		}
	}
	
	public Vector <String> classifyEmotion(double[] featureVector) {
		Vector<DoubleStringPair> emotionObjects = new Vector<DoubleStringPair>();
		emotionObjects = new Vector<DoubleStringPair>();
		for (int i = 0; i < _classifiers.length; i++) {
			String emotion = _classifiers[i].getEmotion();
			double probability = _classifiers[i].getProbabilityOfEmotion(featureVector);
			
			if (probability >= 0.5) {
				DoubleStringPair emotionObject = new DoubleStringPair(probability, emotion);
				emotionObjects.add(emotionObject);
			}
		}
		Collections.sort(emotionObjects, new DoubleStringPair.SortFirstDouble());
		
		Vector <String> emotions = new Vector<String>();
		for (int i = 0; i < emotionObjects.size(); i++) {
			System.out.println(emotionObjects.get(i).getFirst());
			emotions.add(emotionObjects.get(i).getSecond());
		}
		return emotions;
	}
}
