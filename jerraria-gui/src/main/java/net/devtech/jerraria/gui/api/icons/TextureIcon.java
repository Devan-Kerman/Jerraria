package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.ColorTextureShader;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.render.api.textures.Texture;

public record TextureIcon(Texture texture, int color, ShaderKey<ColorTextureShader> key) implements Icon {
	public TextureIcon(Texture texture, int color) {
		this(texture, color, ColorTextureShader.keyFor(texture));
	}

	public TextureIcon(Texture texture) {
		this(texture, 0xFFFFFFFF, ColorTextureShader.keyFor(texture));
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		ColorTextureShader batch = renderer.getBatch(this.key);
		batch.rect(renderer.mat(), this.texture, 0, 0, 1, 1, this.color);
	}

	@Override
	public float aspectRatio() {
		return this.texture.aspectRatio();
	}
}
