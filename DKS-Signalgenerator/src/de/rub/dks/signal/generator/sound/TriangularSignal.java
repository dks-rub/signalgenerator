package de.rub.dks.signal.generator.sound;

public class TriangularSignal extends Signal {

	@Override
	public double calculate(double freq, double phase) {
		return 2 * Math.abs(2 * ((freq + phase) / (2 * Math.PI) - Math.floor((freq + phase) / (2 * Math.PI) + 0.5))) - 1;
	}

}
