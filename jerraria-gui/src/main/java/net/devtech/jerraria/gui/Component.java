package net.devtech.jerraria.gui;

import java.awt.Rectangle;
import java.awt.Shape;

import net.devtech.jerraria.gui.input.ClickType;
import net.devtech.jerraria.gui.input.Mouse;

public abstract class Component {
	public static final Shape ZERO_BOUND = new Rectangle(0, 0, 0, 0);

	public abstract void draw(GuiRenderer renderer, Mouse mouse);

	/**
	 * The expected maximum bounds this component will render to, this can just be a rectangle and is often treated as
	 * one regardless of the real bounds. (eg. if you made a circle, it won't try to hex pack them to optimally tile a
	 * space, it'll just square pack them because no one will bother with the former.)
	 */
	public Shape renderingBounds() {
		return this.eventFilter();
	}

	/**
	 * if an event occurs within a point that the given shape {@link Shape#contains(double, double)}, then it will be
	 * passed
	 */
	public Shape eventFilter() {
		return ZERO_BOUND;
	}

	/**
	 * Fired when a mouse clicks the component
	 * @return if the event was consumed and shouldn't be passed to any other components
	 */
	public boolean mouseClick(Mouse mouse, ClickType type) {
		return false;
	}

	/**
	 * Called when a mouse starts dragging from the current component.
	 */
	public boolean mouseDragStart(Mouse fromMouse, ClickType type) {
		return false;
	}

	/**
	 * Fired when a mouse drags over the component
	 * @param deltaX the distance moved by the mouse from its previous point
	 * @return if the event was consumed and shouldn't be passed to any other components
	 */
	public boolean mouseDrag(Mouse mouse, double deltaX, double deltaY, ClickType type) {
		return false;
	}

	/**
	 * Called when a mouse ends dragging on the current component
	 */
	public boolean mouseDragEnd(Mouse toMouse, ClickType type) {
		return false;
	}
}
