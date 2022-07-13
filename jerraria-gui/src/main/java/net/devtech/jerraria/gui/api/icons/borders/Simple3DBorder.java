package net.devtech.jerraria.gui.api.icons.borders;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.MatView;

public record Simple3DBorder(float width, float height, Settings settings) implements Icon {
	public static final Settings INVERTED = new Settings(0xffffffff, 0xff373737, 0xffaaaaaa, 1);
	public static final Settings DEFAULT = new Settings(0xff373737, 0xffffffff, 0xffaaaaaa, 1);
	public static final Settings DISABLED = new Settings(0xff494949, 0xff2d2d2d, 0xffaaaaaa, 1);
	public static final Settings HIGHLIGHTED_BUTTON = new Settings(0xffcfd0f7, 0xff373860, 0xffaaaacc, 1);

	public record Settings(int topLeft, int bottomRight, int corner, float borderWidth, float borderHeight)
		implements BorderFactory.Simple {
		public Settings(int topLeft, int bottomRight, int corner) {
			this(topLeft, bottomRight, corner, 1, 1);
		}

		public Settings(int topLeft, int bottomRight, int corner, float thickness) {
			this(topLeft, bottomRight, corner, thickness, thickness);
		}

		public Settings with(float borderWidth, float borderHeight) {
			return new Settings(this.topLeft, this.bottomRight, this.corner, borderWidth, borderHeight);
		}

		public Settings with(float thickness) {
			return this.with(thickness, thickness);
		}
	}

	public static final BorderFactory<Settings> FACTORY = BorderFactory.simple(Simple3DBorder::new);

	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		this.draw(renderer, this.width(), this.height());
	}

	@Override
	public void draw(MatrixBatchedRenderer renderer, float width, float height) {
		SolidColorShader batch = renderer.getBatch(SolidColorShader.KEYS.getFor(AutoStrat.QUADS));
		MatView scale = renderer.mat();

		batch.rect(scale, 0, 0, this.settings.borderWidth, height, this.settings.topLeft);
		batch.rect(scale, 0, 0, width, this.settings.borderHeight, this.settings.topLeft);

		batch.rect(scale,
			width - this.settings.borderWidth,
			0,
			this.settings.borderWidth,
			height,
			this.settings.bottomRight
		);
		batch.rect(
			scale,
			0,
			height - this.settings.borderHeight,
			width,
			this.settings.borderHeight,
			this.settings.bottomRight
		);

		batch.rect(scale,
			width - this.settings.borderWidth,
			0,
			this.settings.borderWidth,
			this.settings.borderHeight,
			this.settings.corner
		);
		batch.rect(scale,
			0,
			height - this.settings.borderHeight,
			this.settings.borderWidth,
			this.settings.borderHeight,
			this.settings.corner
		);
	}
}
