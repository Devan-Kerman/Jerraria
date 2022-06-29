package net.devtech.jerraria.gui.api;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;

/**
 * Immediate Mode Gui Renderer. <br> Inspired by <a href="https://docs.unity3d.com/Manual/gui-Basics.html">Unity</a> &
 * <a href="https://github.com/ocornut/imgui">Dear ImGui</a>
 */
public abstract class ImGuiRenderer extends WidgetRenderer {
	/**
	 * Whether the current subsection is building up or down
	 */
	public abstract boolean isVertical();

	public void spacer(int size) {
		if(this.isVertical()) {
			this.drawSpace(0, size);
		} else {
			this.drawSpace(size, 0);
		}
	}

	// todo isClicked & Stuff
	// todo variable sized parts (they attempt to fill the space)

	/**
	 * Creates a new subdivision that does not influence the parent subdivision's dimensions. The {@link #isVertical()}
	 * state is inherited from the parent subdivision.
	 *
	 * @param x the x offset relative to the window's top left corner
	 */
	public abstract SubdivisionStack absolute(float x, float y);

	public SubdivisionStack centered(
		float panelWidth, float panelHeight, float subdivisionWidth, float subdivisionHeight) {
		return this.absolute((panelWidth - subdivisionWidth) / 2, (panelHeight - subdivisionHeight) / 2);
	}

	/**
	 * Creates a subdivision of the current rendering space. A vertical subdivision will add subsequent components
	 * downwards.
	 *
	 * <pre>{@code
	 * // start building a vertical list
	 * try(renderer.vertical().pop) {
	 *      // add one button to the list
	 *      if(renderer.button(10, 10, "Hi!")) System.out.println("Hello!");
	 *      // add another
	 *      if(renderer.button(10, 10, "Hello!")) System.out.println("Good Morning!");
	 * }
	 * }</pre>
	 *
	 * @see SubdivisionStack
	 */
	public abstract SubdivisionStack vertical();

	/**
	 * Creates a subdivision of the current rendering space. A horizontal subdivision will add subsequent components
	 * rightward.
	 *
	 * @see SubdivisionStack
	 */
	public abstract SubdivisionStack horizontal();

	/**
	 * Create a subdivision who's real size does not influence the parent subdivision, so for example, if you create a
	 * fixed size subdivision of 100x100 and put a 200x200 button in it, the parent subdivision will only move the
	 * offset & size by the 100x100
	 *
	 * This is useful in the case where you want to draw a dropdown menu, where you don't want the dropdown to
	 * influence
	 * the main menu.
	 *
	 * <pre>{@code
	 * // ensure the bounds never go past 20x10
	 * // and start building the dropdown
	 * try(renderer.fixedSize(20, 10).pop; renderer.vertical().pop;) {
	 *      // create the menu button
	 *      if(renderer.focusButton(20, 10, "files")) {
	 *          // build the dropdown menu
	 *          renderer.button(40, 10, "delete");
	 *          renderer.button(40, 10, "save as");
	 *          renderer.button(40, 10, "save");
	 *      }
	 * }
	 * }</pre>
	 */
	public SubdivisionStack fixedSize(float width, float height) {
		SubdivisionState reference = this.createReference();
		this.drawSpace(width, height);
		return this.gotoReference(reference);
	}

	/**
	 * Starts building a top level part in the current subdivision. Top level parts are rendered at the minimum Z level
	 * which makes them render above anything else.
	 *
	 * <pre>{@code
	 * try(renderer.elevated().pop) {
	 *     renderer.button(40, 10, "");
	 * }
	 * }</pre>
	 */
	public abstract TopState top();

	/**
	 * Moves the current offset to where the subdivision state was captured. This does not retroactively affect already
	 * rendered components, so for example, if you create a state half-way along a list, and then go back to that
	 * reference and draw a new item in the list, it will just render over the previous items.
	 */
	public abstract SubdivisionStack gotoReference(SubdivisionState reference);

	/**
	 * @return An object that stores the current offset in the current subdivision
	 */
	public abstract SubdivisionState createReference();
}
