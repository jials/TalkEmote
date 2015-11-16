package logisticregression;
import java.util.Comparator;


public class DoubleStringPair {
	public static class SortFirstDouble implements Comparator <DoubleStringPair> { 

		@Override
		public int compare(DoubleStringPair o1, DoubleStringPair o2) {
			if (o2.getFirst() - o1.getFirst() > 0) {
				return -1;
			} 

			return 1;
		}
	}
	
	private double _first;
	private String _second;
	
	public DoubleStringPair(double first, String second) {
		setFirst(first);
		setSecond(second);
	}

	public double getFirst() {
		return _first;
	}

	public void setFirst(double first) {
		this._first = first;
	}

	public String getSecond() {
		return _second;
	}

	public void setSecond(String second) {
		this._second = second;
	}
}
