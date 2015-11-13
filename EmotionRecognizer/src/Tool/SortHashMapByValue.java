package Tool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;

public class SortHashMapByValue {
	private int topN = 50;
	
	public SortHashMapByValue(int topN){
		this.topN = topN;
	}
	
	public ArrayList<String> sort(HashMap<String, Double> map){
		ArrayList<String> rankList = new ArrayList<String>();
		HashMap<Double, ArrayList<String>> reversMap = reversMap(map);
		ArrayList<Double> valueList = new ArrayList<Double>();
		valueList.addAll(reversMap.keySet());
		Collections.sort(valueList);
		for(int i = 0; i < valueList.size(); i++){
			double v = valueList.get(i);
			ArrayList<String> list = reversMap.get(v);
			for(int j = 0; j < list.size(); j++){
				String key = list.get(j);
				rankList.add(key);
				if (rankList.size() == topN){
					break;
				}
			}
			if (rankList.size() == topN){
				break;
			}
		}
		return rankList;
	}

	private HashMap<Double, ArrayList<String>> reversMap(
			HashMap<String, Double> map) {
		// TODO Auto-generated method stub
		HashMap<Double, ArrayList<String>> revMap = new HashMap<Double, ArrayList<String>>();
		Iterator<String> it = map.keySet().iterator();
		while(it.hasNext()){
			String key = it.next();
			double value = map.get(key);
			if(revMap.containsKey(value)){
				ArrayList<String> list = revMap.get(value);
				list.add(key);
				revMap.put(value, list);
			}else{
				ArrayList<String> list = new ArrayList<String>();
				list.add(key);
				revMap.put(value, list);
			}
		}
		
		return revMap;
	}

}
