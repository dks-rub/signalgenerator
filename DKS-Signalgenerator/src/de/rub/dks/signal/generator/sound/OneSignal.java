package de.rub.dks.signal.generator.sound;

public class OneSignal extends Signal {

	@Override
	public double calculate(double freq, double phase) {
		return 1;
	}

}
