package de.rub.dks.signal.generator.sound.arithmetic;

import com.jjoe64.graphview.GraphView.GraphViewData;

import de.rub.dks.signal.generator.sound.Signal;

/*
* Advanced Signal class for operations on two Signals.
* Extend this class and implement the calculate method which is used by all the other methods.
*/
public abstract class ArithmeticSignal {
	protected Signal a, b;
	protected double audioPosA, audioPosB;

	public ArithmeticSignal(Signal aa, Signal bb) {
		setOperands(aa, bb);
	}

	/**
     	* Calculates the desired operation of the two operand's values at given frequency and phase
     	* @param freq1  the first signal's frequency
     	* @param phase1	the first signal's phase
     	* @param freq2  the seconds signal's frequency
     	* @param phase2	the seconds signal's phase
     	*/
	public abstract double calculate(double freq1, double phase1, double freq2, double phase2);

	/**
     	* Generates a GraphViewData array with Signal.GRAPH_SAMPLES entries.
     	* This method uses the internal phase and frequency values of its operand members.
     	* @return    	the samples
     	*/
	public GraphViewData[] getGraphData() {
		GraphViewData[] data = new GraphViewData[Signal.GRAPH_SAMPLES];
		double x = 0, y = 0;
		double stepX = a.getFreq() * 3 / Signal.GRAPH_SAMPLES;
		double stepY = b.getFreq() * 3 / Signal.GRAPH_SAMPLES;
		for (int i = 0; i < Signal.GRAPH_SAMPLES; i++) {
			data[i] = new GraphViewData(i, calculate(x, a.getPhase(), y, b.getPhase()));
			x += stepX;
			y += stepY;
		}
		return data;
	}

	/**
     	* Generates a byte array with Signal.BUFFER_SIZE samples to be played back, resembling the sound of the signal.
     	* This method uses the internal phase and frequency values of its operand members.
     	* @return    	the samples
     	*/
	public byte[] getAudioBytes() {
		byte[] samples = new byte[Signal.BUFFER_SIZE];
		for (int i = 0; i < Signal.BUFFER_SIZE; i++) {
			samples[i] = (byte) (Signal.AMPLITUDE * calculate(audioPosA, a.getPhase(), audioPosB, b.getPhase()));
			// 2 * Math.PI * freq / SAMPLE_RATE;
			audioPosA += 100 * a.getFreq() / Signal.SAMPLE_RATE;
			audioPosB += 100 * b.getFreq() / Signal.SAMPLE_RATE;
		}
		return samples;
	}
	
	// Getters and setters for the internal variables

	public void setOperands(Signal aa, Signal bb) {
		a = aa;
		b = bb;
	}

	public void setSignalA(Signal aa) {
		a = aa;
	}

	public void setSignalB(Signal bb) {
		b = bb;
	}

	public Signal getSignalA() {
		return a;
	}

	public Signal getSignalB() {
		return b;
	}

}
