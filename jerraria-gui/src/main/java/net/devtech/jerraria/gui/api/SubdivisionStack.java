package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.util.SafeClosable;

/**
 * Represents the stack of subdivisions of an ImGuiRenderer
 * <pre>{@code
 *      ImGuiRenderer renderer = ...;
 *      try(renderer.vertical(100, 100).self) {
 *          // create your gui in this subdivision
 *      }
 * }</pre>
 */
public abstract class SubdivisionStack {
	/**
	 * An AutoClosable that when closed, pops the latest subdivision off the stack
	 */
	public final SafeClosable self = this::pop;

	protected abstract void pop();
}
