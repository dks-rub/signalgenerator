package de.rub.dks.signal.generator.sound;

public class SinusSignal extends Signal {

	public double calculate(double freq, double phase) {
		return Math.sin(freq + phase);
	}


}
