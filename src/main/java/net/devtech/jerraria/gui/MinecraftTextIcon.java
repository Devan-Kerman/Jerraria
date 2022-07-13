package net.devtech.jerraria.gui;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;

public record MinecraftTextIcon(TextRenderer renderer, Text text, int color, boolean shadow, boolean translucent, int backgroundColor, int light, @Nullable Integer outlineColor) implements Icon {
	@Override
	public float width() {
		return this.renderer.getWidth(this.text);
	}

	@Override
	public float height() {
		return 9;
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		if(this.outlineColor != null) {

		}
	}
}
