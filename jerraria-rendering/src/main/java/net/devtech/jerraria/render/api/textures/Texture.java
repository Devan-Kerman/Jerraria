package net.devtech.jerraria.render.api.textures;

/**
 * A texture or fraction of a gl texture
 */
public final class Texture {
	final int glId;
	final float offX, offY, width, height;

	public Texture(int id, float x, float y, float width, float height) {
		this.glId = id;
		this.offX = x;
		this.offY = y;
		this.width = width;
		this.height = height;
	}

	public Texture section(float offX, float offY, float width, float height) {
		return new Texture(
			this.glId,
			this.offX + offX * this.width,
			this.offY + offY * this.width,
			width * this.width,
			height * this.height
		);
	}

	public int getGlId() {
		return this.glId;
	}

	/**
	 * @return normalized [0-1] offset
	 */
	public float getOffX() {
		return this.offX;
	}

	public float getOffY() {
		return this.offY;
	}

	public float getFudgedOffX() {
		return this.offX + .001f;
	}

	public float getFudgedOffY() {
		return this.offY + .001f;
	}

	public float getFudgedWidth() {
		return this.width - .002f;
	}

	public float getFudgedHeight() {
		return this.height - .002f;
	}

	/**
	 * @return normalized [0-1] width
	 */
	public float getWidth() {
		return this.width;
	}

	public float getHeight() {
		return this.height;
	}
}
