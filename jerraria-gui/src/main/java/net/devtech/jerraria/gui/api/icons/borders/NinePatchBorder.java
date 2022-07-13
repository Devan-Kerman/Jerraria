package net.devtech.jerraria.gui.api.icons.borders;

import juuxel.libninepatch.TextureRegion;
import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.render.api.textures.Texture;

public record NinePatchBorder(NinePatch<Integer> patch, float width0, float height0) implements Icon {
	public static final BorderFactory<NinePatch<Integer>> FACTORY = (icon, settings) -> {
		float borderWidth = settings.cornerWidth, borderHeight = settings.cornerHeight;
		float combinedWidth = icon.width() + borderWidth * 2;
		float combinedHeight = icon.height() + borderHeight * 2;
		Icon border = new NinePatchBorder(settings, combinedWidth, combinedHeight);
		return border.overlay(BorderFactory.shrink(icon, borderWidth, borderHeight));
	};

	public static NinePatch.Builder<Integer> patch(Texture texture) {
		return NinePatch.builder(new TextureRegion<>(
			texture.getGlId(),
			texture.getFudgedOffX(),
			texture.getFudgedOffY(),
			texture.getFudgedOffX() + texture.getFudgedWidth(),
			texture.getFudgedOffY() + texture.getFudgedHeight()
		));
	}

	@Override
	public float width() {
		return this.width0;
	}

	@Override
	public float height() {
		return this.height0;
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		this.draw(renderer, this.width0, this.height0);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
		NinePatchTextureRenderer tex = new NinePatchTextureRenderer(renderer);
		this.patch.draw(tex, (int)dimX, (int)dimY);
	}
}
