package net.devtech.jerraria.gui.api.themes.x16;

import net.devtech.jerraria.gui.api.MatrixBatchedRenderer;
import net.devtech.jerraria.gui.api.icons.Icon;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.math.Mat;

record ButtonBackground(float width, float height, State state) implements Icon {
	@Override
	public void draw(MatrixBatchedRenderer renderer) {
		SolidColorShader batch = renderer.getBatch(SolidColorShader.KEYS.getFor(AutoStrat.QUADS));
		Mat scale = renderer.mat().copy().scale(1 / this.width, 1 / this.height);

		batch.rect(scale, 0, 0, this.width, this.height, this.state.background);

		batch.rect(scale, 0, 0, 1, this.height, this.state.topLeft);
		batch.rect(scale, 0, 0, this.width, 1, this.state.topLeft);

		batch.rect(scale, this.width - 1, 0, 1, this.height, this.state.bottomRight);
		batch.rect(scale, 0, this.height - 1, this.width, 1, this.state.bottomRight);

		batch.rect(scale, this.width - 1, 0, 1, 1, this.state.corner);
		batch.rect(scale, 0, this.height - 1, 1, 1, this.state.corner);
	}

	@Override
	public float aspectRatio() {
		return this.width / this.height;
	}

	public enum State {
		INVERTED(0xff8b8b8b, 0xffffffff, 0xff373737, 0xffaaaaaa),
		DEFAULT(0xff8b8b8b, 0xff373737, 0xffffffff, 0xffaaaaaa),
		DISABLED(0xff373737, 0xff494949, 0xff2d2d2d, 0xffaaaaaa),

		/**
		 * The button in the beacon inventory uses the exact same colors/texture as regular slots, except it's highlight texture is different
		 */
		HIGHLIGHTED_BUTTON(0xff7778a0, 0xffcfd0f7, 0xff373860, 0xffaaaacc);

		final int background, topLeft, bottomRight, corner;

		State(int background, int topLeft, int bottomRight, int corner) {
			this.background = background;
			this.topLeft = topLeft;
			this.bottomRight = bottomRight;
			this.corner = corner;
		}
	}
}
