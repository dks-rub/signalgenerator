package de.rub.dks.signal.generator.sound.arithmetic;

import de.rub.dks.signal.generator.sound.Signal;

public class DifferenceSignal extends ArithmeticSignal {

	public DifferenceSignal(Signal aa, Signal bb) {
		super(aa, bb);
	}


	public double calculate(double freq1, double phase1, double freq2, double phase2) {
		return a.calculate(freq1, phase1) - b.calculate(freq2, phase2);
	}

}