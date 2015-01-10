package de.rub.dks.signal.generator.sound;

public class ZeroSignal extends Signal {

	@Override
	public double calculate(double freq, double phase) {
		return 0;
	}

}
