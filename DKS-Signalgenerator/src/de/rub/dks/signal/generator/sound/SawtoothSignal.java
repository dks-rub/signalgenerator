package de.rub.dks.signal.generator.sound;

public class SawtoothSignal extends Signal {

	@Override
	public double calculate(double freq, double phase) {
		return 2 * ((freq + phase) / (2 * Math.PI) - Math.floor(0.5 + (freq + phase) / (2 * Math.PI)));
	}

}
