package net.devtech.jerraria.gui.api.input;

import java.util.Set;

public interface KeyInputProcessor {
	/**
	 * Insert the given codepoints into the current position, wherever that may be.
	 */
	void characterInput(int codePoint);

	/**
	 * Non character inputs, you should use {@link #characterInput(int)} to read input data and not this.
	 *
	 * @param uniqueKeyId also known as the "scancode" in glfw
	 */
	void keyboardInput(Key key, int uniqueKeyId, Action action, Set<Modifier> modifiers);

	enum Action {
		/**
		 * Pressed when the user first presses the key.
		 */
		PRESS,
		/**
		 * If the input was the user releasing the key.
		 */
		RELEASE,
		/**
		 * If this event was fired because the user is holding down the key (for example, holding down the 'a' key will
		 * print lots of 'a's). The other two events are still called before and after the user holds down the key.
		 */
		REPEAT
	}
}
