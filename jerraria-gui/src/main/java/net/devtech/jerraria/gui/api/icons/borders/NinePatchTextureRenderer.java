package net.devtech.jerraria.gui.api.icons.borders;

import juuxel.libninepatch.TextureRenderer;
import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.ColorTextureShader;
import net.devtech.jerraria.render.api.textures.Texture;

public class NinePatchTextureRenderer implements TextureRenderer<Integer> {
	final MatrixBatchedRenderer renderer;

	public NinePatchTextureRenderer(MatrixBatchedRenderer renderer) {
		this.renderer = renderer;
	}

	@Override
	public void draw(Integer glId, int x, int y, int width, int height, float u1, float v1, float u2, float v2) {
		Texture texture = new Texture(glId::intValue, Math.min(u1, u2), Math.min(v1, v2), Math.abs(u2-u1), Math.abs(v2-v1));
		ColorTextureShader batch = this.renderer.getBatch(ColorTextureShader.keyFor(texture));
		batch.rect(this.renderer.mat(), texture, x, y, width, height, 0xFFFFFFFF);
	}
}
