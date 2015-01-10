package de.rub.dks.signal.generator.sound;

public class RectangularSignal extends Signal{

	@Override
	public double calculate(double freq, double phase) {
		return Math.signum(Math.sin(freq + phase));
	}

}
