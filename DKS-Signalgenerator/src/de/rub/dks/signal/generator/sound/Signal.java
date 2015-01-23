package de.rub.dks.signal.generator.sound;

import com.jjoe64.graphview.GraphView.GraphViewData;

public abstract class Signal {
	public static final int GRAPH_SAMPLES = 800;
	public static final int BUFFER_SIZE = 4096;
	public static final int SAMPLE_RATE = 44100;
	public static final byte AMPLITUDE = Byte.MAX_VALUE;

	protected double audioPos, fr, ph;

	public double getFreq() {
		return fr;
	}

	public void setFreq(double fr) {
		this.fr = fr;
	}

	public double getPhase() {
		return ph;
	}

	public void setPhase(double ph) {
		this.ph = ph;
	}

	public abstract double calculate(double freq, double phase);

	public double calculate() {
		return calculate(fr, ph);
	}

	public GraphViewData[] getGraphData(double freq, double phase) {
		fr = freq;
		ph = phase;
		GraphViewData[] data = new GraphViewData[GRAPH_SAMPLES];
		double x = 0;
		double step = freq * 3 / GRAPH_SAMPLES;
		for (int i = 0; i < GRAPH_SAMPLES; i++) {
			data[i] = new GraphViewData(i, calculate(x, phase));
			x += step;
		}
		return data;
	}

	public byte[] getAudioBytes(double freq, double phase) {
		fr = freq;
		ph = phase;
		byte[] samples = new byte[BUFFER_SIZE];
		for (int i = 0; i < BUFFER_SIZE; i++) {
			samples[i] = (byte) (AMPLITUDE * calculate(audioPos, phase));
			// audioPos += 2 * Math.PI * freq / SAMPLE_RATE;
			audioPos += 440 * freq / SAMPLE_RATE;
		}
		return samples;
	}

}
