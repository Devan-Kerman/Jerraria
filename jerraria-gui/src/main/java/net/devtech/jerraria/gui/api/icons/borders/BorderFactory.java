package net.devtech.jerraria.gui.api.icons.borders;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatCacheEntry;

public interface BorderFactory<T> {

	Icon createBordered(Icon icon, T settings);

	interface Simple {
		float borderWidth();
		float borderHeight();
	}

	interface SimpleBorderFactory<T extends Simple> {
		Icon create(float width, float height, T settings);
	}

	static <T extends Simple> BorderFactory<T> simple(SimpleBorderFactory<T> factory) {
		return (icon, settings) -> {
			float borderWidth = settings.borderWidth(), borderHeight = settings.borderHeight();
			float combinedWidth = icon.width() + borderWidth * 2;
			float combinedHeight = icon.height() + borderHeight * 2;
			Icon border = factory.create(combinedWidth, combinedHeight, settings);
			return border.overlay(BorderFactory.shrink(icon, borderWidth, borderHeight));
		};
	}

	static Icon shrink(Icon icon, float borderWidth, float borderHeight) {
		float combinedWidth = icon.width() + borderWidth * 2;
		float combinedHeight = icon.height() + borderHeight * 2;
		return new Icon.Forwarding(icon) {
			@Override
			public void draw(MatrixBatchedRenderer renderer) {
				try(MatCacheEntry copy = POOL.copy(renderer.mat())) {
					Mat instance = copy.instance().offset(borderWidth, borderHeight).scale(this.width() / combinedWidth, this.height() / combinedHeight);
					this.inherit.draw(renderer.withMat(instance));
				}
			}

			@Override
			public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
				try(MatCacheEntry copy = POOL.copy(renderer.mat())) {
					float width = dimX - borderWidth * 2, height = dimY - borderHeight * 2;
					Mat instance = copy.instance().offset(borderWidth, borderHeight).scale(width / dimX, height / dimY);
					this.inherit.draw(renderer.withMat(instance), dimX, dimY);
				}
			}
		};
	}
}
