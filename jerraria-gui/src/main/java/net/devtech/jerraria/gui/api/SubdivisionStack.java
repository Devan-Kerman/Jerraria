package net.devtech.jerraria.gui.api;

/**
 * Represents the stack of subdivisions of an ImGuiRenderer
 * <pre>{@code
 *      ImGuiRenderer renderer = ...;
 *      try(renderer.vertical(100, 100).pop) {
 *          // create your gui in this subdivision
 *      }
 * }</pre>
 */
public abstract class SubdivisionStack {
	/**
	 * An AutoClosable that when closed, pops the latest subdivision off the stack
	 */
	public final PopStack pop;

	protected SubdivisionStack() {
		this.pop = this::pop;
	}

	protected abstract void pop();

	public interface PopStack extends AutoCloseable {
		@Override
		void close();
	}
}
