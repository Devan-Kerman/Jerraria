package net.devtech.jerraria.gui;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.util.math.Mat4f;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.text.Text;
import net.minecraft.util.math.Matrix4f;

public record TextIcon(
	TextRenderer renderer,
	Text text,
	int color,
	boolean shadow,
	boolean translucent,
	int backgroundColor,
	int light,
	@Nullable Integer outlineColor
) implements Icon {
	public static TextIconBuilder builder() {
		return new TextIconBuilder();
	}

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
		Matrix4f mat = ((Mat4f) renderer.mat()).mat;
		if(this.outlineColor == null) {
			// todo matrix & vertex consumers
			this.renderer.draw(this.text,
				0,
				0,
				this.color,
				this.shadow,
				mat,
				null,
				this.translucent,
				this.backgroundColor,
				this.light
			);
		} else {
			this.renderer.drawWithOutline(this.text.asOrderedText(),
				0,
				0,
				this.color,
				this.outlineColor,
				mat,
				null,
				this.light
			);
		}
	}
}
