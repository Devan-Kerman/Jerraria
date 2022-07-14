package net.devtech.jerraria.gui;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.text.Text;

public class TextIconBuilder {
	int color = 0xFFFFFFFF;
	boolean shadow = true;
	boolean translucent = false;
	int backgroundColor = 0;
	int light = LightmapTextureManager.MAX_LIGHT_COORDINATE;
	Integer outlineColor;

	public TextIconBuilder() {
	}

	public int getColor() {
		return this.color;
	}

	public TextIconBuilder color(int color) {
		this.color = color;
		return this;
	}

	public boolean isShadow() {
		return this.shadow;
	}

	public TextIconBuilder shadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	public boolean isTranslucent() {
		return this.translucent;
	}

	public TextIconBuilder translucent(boolean translucent) {
		this.translucent = translucent;
		return this;
	}

	public int getBackgroundColor() {
		return this.backgroundColor;
	}

	public TextIconBuilder backgroundColor(int backgroundColor) {
		this.backgroundColor = backgroundColor;
		return this;
	}

	public int getLight() {
		return this.light;
	}

	public TextIconBuilder light(int light) {
		this.light = light;
		return this;
	}

	public Integer getOutlineColor() {
		return this.outlineColor;
	}

	public TextIconBuilder outlineColor(@Nullable Integer outlineColor) {
		this.outlineColor = outlineColor;
		return this;
	}

	public TextIcon build(Text text) {
		return new TextIcon(
			MinecraftClient.getInstance().textRenderer,
			text,
			this.color,
			this.shadow,
			this.translucent,
			this.backgroundColor,
			this.light,
			this.outlineColor
		);
	}
}
