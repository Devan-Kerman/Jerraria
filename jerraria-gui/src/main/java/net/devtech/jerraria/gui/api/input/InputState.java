package net.devtech.jerraria.gui.api.input;

import java.util.Set;

import net.devtech.jerraria.gui.api.WidgetRenderer;

public abstract class InputState {
	protected abstract WidgetRenderer renderer();

	/**
	 * @return The mouse's relative position to the current component
	 */
	public abstract float mouseX();
	public abstract float mouseY();

	public abstract boolean isPressed(MouseButton type);

	/**
	 * @return true if the given mouse button was pressed in the previous frame
	 */
	public abstract boolean wasPressed(MouseButton type);

	public abstract Set<Modifier> pressedModifiers(MouseButton type);

	/**
	 * @return The modifiers used if the button was pressed from the previous frame
	 */
	public abstract Set<Modifier> previousModifiers(MouseButton type);

	public boolean isPressedInBounds(MouseButton button) {
		if(this.isPressed(button)) {
			return this.isMouseInBounds();
		}
		return false;
	}

	public boolean isMouseInBounds() {
		float mx = this.mouseX(), my = this.mouseY();
		return this.isPositionInBounds(mx, my);
	}

	/**
	 * @return true if the position is in the current {@link WidgetRenderer#drawSpace(float, float)}
	 */
	public boolean isPositionInBounds(float mx, float my) {
		WidgetRenderer renderer = this.renderer();
		if(mx < 0 || mx >= renderer.drawSpaceWidth()) {
			return false;
		}

		if(my < 0 || my >= renderer.drawSpaceHeight()) {
			return false;
		}
		return true;
	}
}
