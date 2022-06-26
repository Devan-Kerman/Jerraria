package net.devtech.jerraria.gui.api;

import java.awt.geom.Rectangle2D;

public abstract class MouseState {
	protected abstract WidgetRenderer renderer();

	public abstract float mouseX();

	public abstract float mouseY();

	public abstract boolean isPressed(); // todo flags / mouse type

	public boolean isPressedInCurrentDrawState() {
		if(this.isPressed()) {
			// todo fix
		}
		return false;
	}
}
