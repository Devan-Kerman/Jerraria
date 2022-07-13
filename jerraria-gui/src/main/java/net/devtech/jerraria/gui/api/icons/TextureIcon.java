package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.ColorTextureShader;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.render.api.textures.Texture;

public record TextureIcon(Texture texture, int color, ShaderKey<ColorTextureShader> key, float width, float height) implements Icon {
	public TextureIcon(Texture texture, int color, float width, float height) {
		this(texture, color, ColorTextureShader.keyFor(texture), width, height);
	}

	public TextureIcon(Texture texture, float width, float height) {
		this(texture, 0xFFFFFFFF, width, height);
	}

	public TextureIcon(Texture texture, int color, float height) {
		this(texture, color, texture.aspectRatio() * height, height);
	}

	public TextureIcon(Texture texture, float height) {
		this(texture, texture.aspectRatio() * height, height);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		ColorTextureShader batch = renderer.getBatch(this.key);
		batch.rect(renderer.mat(), this.texture, 0, 0, this.width(), this.height(), this.color);
	}
}
