package signal;

import java.util.Vector;

import javax.sound.sampled.AudioFormat;

public class WavObj {
	private static final double TIME_SILENCE_SPLIT_INTERVAL = 1;
	private AudioFormat _wavFormat;
	private long _audioFileLength;
	private short[] _signal;
	private static final double TIME_TURN = 4.5;
	private static final int SHORT_TO_BYTE = 2;
	
	public WavObj(AudioFormat wavFormat, 
			      long audioFileLength,
			      short[] signal) {
		_wavFormat = wavFormat;
		_audioFileLength = audioFileLength;
		_signal = signal;
	}
	
	public AudioFormat getFormat() {
		return _wavFormat;
	}
	
	public long getFileLength() {
		return _audioFileLength;
	}
	
	public short[] getSignal() {
		return _signal;
	}
	
	public int getFrameSize() {
		return _wavFormat.getFrameSize();
	}
	
	public float getFrameRate() {
		return _wavFormat.getFrameRate();
	}
	
	public double getFileDuration() {
		long audioFileLength = _audioFileLength;
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
        double durationInSeconds = (audioFileLength / (frameSize * frameRate));
		return durationInSeconds;
	}
	
	public int getNumOfFrames() {
		long audioFileLength = _audioFileLength;
		int frameSize = _wavFormat.getFrameSize();
        int numOfFrames = (int) Math.ceil((double) audioFileLength / (double) frameSize);
        return numOfFrames;
	}
	
	public short[] trimSilence(short[] originalSignal) {
		originalSignal = trimSilenceBegining(originalSignal);
		originalSignal = trimSilenceEnd(originalSignal);
		return originalSignal;
	}
	
	public short[] trimSilenceEnd(short[] originalSignal) {
		originalSignal = reverse(originalSignal);
		originalSignal = trimSilenceBegining(originalSignal);
		originalSignal = reverse(originalSignal);
		return originalSignal;
	}
	
	public short[] reverse(short[] originalSignal) {
		for (int i = 0; i < originalSignal.length / 2; i++) {
		  short temp = originalSignal[i];
		  originalSignal[i] = originalSignal[originalSignal.length - 1 - i];
		  originalSignal[originalSignal.length - 1 - i] = temp;
		}
		return originalSignal;
	}
	
	public short[] trimSilenceBegining(short[] originalSignal) {
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
        double durationInSeconds = (originalSignal.length * 2 / (frameSize * frameRate));
		
		double threshold = 250;
		double time = 0;
		while (true) {
			double splitIntervalTime = WavObj.TIME_SILENCE_SPLIT_INTERVAL;
			time += WavObj.TIME_SILENCE_SPLIT_INTERVAL;
			short[] samples = getSignalWithFirstFewSeconds(splitIntervalTime, originalSignal);
			double sumOfSquares = 0;
		    for (int i = 0; i < samples.length; i++) {
		        sumOfSquares = sumOfSquares + samples[i] * samples[i];
		    }
		    int numberOfSamples = samples.length;
		    double rms = Math.sqrt(sumOfSquares / (numberOfSamples));
		    
		    if (rms - threshold >= 0 || time >= durationInSeconds) {
		    	break;
		    }
		    
		    originalSignal = getSignalWithoutFirstFewSeconds(splitIntervalTime, originalSignal);
		} 
		return originalSignal;
	}
	
	public boolean isShort(short[] signal) {
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
        double durationInSeconds = (signal.length * 2 / (frameSize * frameRate));
        
        return durationInSeconds - 1 < 0;
	}
	
	public void removeSignalsWithinSeconds(double second) {
		_signal = getSignalWithoutFirstFewSeconds(second);
	}
	
	public short[] getSignalWithFirstFewSeconds(double second) {
		return getSignalWithFirstFewSeconds(second, _signal);
	}
	
	public short[] getSignalWithFirstFewSeconds(double second, short[] originalSignal) {
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
        double durationInSeconds = (originalSignal.length * 2 / (frameSize * frameRate));
        
        if (durationInSeconds <= second) {
        	return originalSignal;
        }
        
		int fileLengthForTurn = (int) (frameSize * frameRate * second / SHORT_TO_BYTE);
		short[] tempSignal = new short[fileLengthForTurn];
		for (int i = 0; i < tempSignal.length; i++) {
			tempSignal[i] = originalSignal[i];
		}
		return tempSignal;
	}
	
	public short[] getSignalWithoutFirstFewSeconds(double second) {
		return getSignalWithoutFirstFewSeconds(second, _signal);
	}
	
	public short[] getSignalWithoutFirstFewSeconds(double second, short[] originalSignal) {
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
        double durationInSeconds = (originalSignal.length * 2 / (frameSize * frameRate));
        
        if (durationInSeconds <= second) {
        	return originalSignal;
        }
        
		int fileLengthForTurn = (int) (frameSize * frameRate * second / SHORT_TO_BYTE);
		short[] tempSignal = new short[originalSignal.length - fileLengthForTurn];
		for (int i = 0; i < tempSignal.length; i++) {
			tempSignal[i] = originalSignal[i + fileLengthForTurn];
		}
		return tempSignal;
	}
	
	public Vector<short[]> splitToSignals() {
		return splitToSignals(TIME_TURN);
	}

	/**
	 * @return
	 */
	public Vector<short[]> splitToSignals(double turnTime) {
		int frameSize = _wavFormat.getFrameSize();
        float frameRate = _wavFormat.getFrameRate();
		int fileLengthForTurn = (int) (frameSize * frameRate * turnTime / SHORT_TO_BYTE);
		int fileLengthEachSignal = _signal.length;
		int index = 0;
		
		Vector<short[]> signals = new Vector<short[]>();
		while (fileLengthEachSignal > 0) {
			if (fileLengthEachSignal >= fileLengthForTurn) {
				short[] curTurnSignal = new short[fileLengthForTurn];
				for (int i = index; i < index + fileLengthForTurn; i++) {
					curTurnSignal[i % fileLengthForTurn] = _signal[i];
				}
				signals.add(curTurnSignal);
				index += fileLengthForTurn;
			} else {
				short[] curTurnSignal = new short[fileLengthEachSignal];
				for (int i = index; i < index + fileLengthEachSignal; i++) {
					curTurnSignal[i % fileLengthForTurn] = _signal[i];
				}
				signals.add(curTurnSignal);
				index += fileLengthEachSignal;
			}
			fileLengthEachSignal -= fileLengthForTurn;
		}
		return signals;
	}
	
}
