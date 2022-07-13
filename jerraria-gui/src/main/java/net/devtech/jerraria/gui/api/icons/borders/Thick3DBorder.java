package net.devtech.jerraria.gui.api.icons.borders;


import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.MatCacheEntry;
import net.devtech.jerraria.util.math.MatView;

public record Thick3DBorder(float width, float height, float scale) implements Icon {
	public record Scale(float scale) implements BorderFactory.Simple {
		@Override
		public float borderWidth() {
			return 4 * this.scale;
		}

		@Override
		public float borderHeight() {
			return 4 * this.scale;
		}
	}

	Thick3DBorder(float width, float height, Scale cfg) {
		this(width, height, cfg.scale);
	}

	public static final BorderFactory<Scale> FACTORY = BorderFactory.simple(Thick3DBorder::new);

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		this.draw(renderer, this.width, this.height);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer, float width, float height) {
		SolidColorShader batch = renderer.getBatch(SolidColorShader.KEYS.getFor(AutoStrat.QUADS));
		try(MatCacheEntry entry = Icon.POOL.copy(renderer.mat())) {
			MatView scale = entry.instance().scale(this.scale, this.scale);
			width /= this.scale;
			height /= this.scale;

			// the background part
			batch.rect(scale, 2, 2, width - 4, height - 4, 0xffc6c6c6);

			// the top shiny part
			batch.rect(scale, 2, 1, width - 5, 2, 0xffffffff);
			// the left shiny part
			batch.rect(scale, 1, 2, 2, height - 5, 0xffffffff);
			// that one pixel in the top left
			batch.rect(scale, 3, 3, 1, 1, 0xffffffff);
			// the right shadow
			batch.rect(scale, width - 3, 3, 2, height - 5, 0xff555555);
			// the bottom shadow
			batch.rect(scale, 3, height - 3, width - 5, 2, 0xff555555);
			// that one pixel in the bottom right
			batch.rect(scale, width - 4, height - 4, 1, 1, 0xff555555);
			// the border
			batch.rect(scale, 0, 2, 1, height - 5, 0xff000000);
			batch.rect(scale, 1, 1, 1, 1, 0xff000000);
			batch.rect(scale, 2, 0, width - 5, 1, 0xff000000);
			batch.rect(scale, width - 3, 1, 1, 1, 0xff000000);
			batch.rect(scale, width - 2, 2, 1, 1, 0xff000000);
			batch.rect(scale, width - 1, 3, 1, height - 5, 0xff000000);
			batch.rect(scale, 1, height - 3, 1, 1, 0xff000000);
			batch.rect(scale, 2, height - 2, 1, 1, 0xff000000);
			batch.rect(scale, 3, height - 1, width - 5, 1, 0xff000000);
			batch.rect(scale, width - 2, height - 2, 1, 1, 0xff000000);
		}
	}
}
