package logisticregression;

public class LogisticMulticlassTrain {
	public static final String EXT_MODEL = ".model";
	private String[] _tags = null;
	private Sctrain[] _trains = null;
	
	/**
	 * 
	 * @param tags should not include "others"
	 */
	public LogisticMulticlassTrain(String[] tags, String trainfile) {
		_tags = tags;
		_trains = new Sctrain[_tags.length];
		
		for (int i = 0; i < _trains.length; i++) {
			_trains[i] = new Sctrain(_tags[i], trainfile, _tags[i] + ".model");
		}
	}
	
	public boolean train() {
		for (int i = 0; i < _trains.length; i++) {
			if (!_trains[i].startTraining()) {
				return false;
			}
		}
		return true;
	}
}
