package net.devtech.jerraria.gui.api.icons.borders;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.MatView;

public record SolidColorBorder(float width, float height, Settings settings) implements Icon {
	public record Settings(int argb, float borderWidth, float borderHeight) implements BorderFactory.Simple {
		public Settings(int argb) {
			this(argb, 1, 1);
		}

		public Settings(int argb, float borderWidth) {
			this(argb, borderWidth, borderWidth);
		}
	}

	public static final BorderFactory<Settings> BORDERED = BorderFactory.simple(SolidColorBorder::new);

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		this.draw(renderer, this.width, this.height);
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer, float dimX, float dimY) {
		SolidColorShader batch = renderer.getBatch(SolidColorShader.KEYS.getFor(AutoStrat.QUADS));
		MatView scale = renderer.mat();
		float bwidth = this.settings.borderWidth(), bheight = this.settings.borderHeight();
		batch.rect(scale, 0, 0, bwidth, dimY, this.settings.argb);
		batch.rect(scale, 0, 0, dimX, bheight, this.settings.argb);

		batch.rect(scale, dimX - bwidth, 0, bwidth, dimY, this.settings.argb);
		batch.rect(scale, 0, dimY - bheight, dimX, bheight, this.settings.argb);

		batch.rect(scale, dimX - bwidth, 0, bwidth, bheight, this.settings.argb);
		batch.rect(scale, 0, dimY - bheight, bwidth, bheight, this.settings.argb);
	}
}
