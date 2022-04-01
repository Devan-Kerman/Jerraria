package net.devtech.jerraria.render.textures;

public final class Texture {
	final int glId;
	final float offX, offY, width, height;

	Texture(int id, float x, float y, float width, float height) {
		this.glId = id;
		this.offX = x;
		this.offY = y;
		this.width = width;
		this.height = height;
	}
}
