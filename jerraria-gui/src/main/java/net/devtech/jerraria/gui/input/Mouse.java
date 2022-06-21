package net.devtech.jerraria.gui.input;

public interface Mouse {
	/**
	 * @return The x coordinate of the mouse relative to this component, for example if it is to it's left, it will be negative.
	 */
	double relativeX();

	double relativeY();
}
