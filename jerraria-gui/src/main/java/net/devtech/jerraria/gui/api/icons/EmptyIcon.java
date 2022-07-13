package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;

public record EmptyIcon(float width, float height) implements Icon {
	public static final EmptyIcon UNIT = new EmptyIcon(1, 1);

	@Override
	public void draw(MatrixBatchedRenderer renderer) {

	}
}
