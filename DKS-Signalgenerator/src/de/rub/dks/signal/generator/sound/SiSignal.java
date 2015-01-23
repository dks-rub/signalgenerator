package de.rub.dks.signal.generator.sound;

public class SiSignal extends Signal {

	@Override
	public double calculate(double freq, double phase) {
		return Math.sin(2 * Math.PI * (freq - phase)) / (2 * Math.PI * (freq - phase));
	}

}
