package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.render.api.batch.BatchedRenderer;
import net.devtech.jerraria.util.math.MatView;

public interface Icon {
	void draw(BatchedRenderer renderer, MatView matrix);

	default Icon darkened() {
		return this.andThen(new OverlayColorIcon(0xAA000000));
	}

	default Icon lightened() {
		return this.andThen(new OverlayColorIcon(0xAAFFFFFF));
	}

	default Icon highlighted() {
		return this.andThen(new OverlayColorIcon(0xAA7777FF));
	}

	default Icon andThen(Icon icon) {
		return (renderer, matrix) -> {
			this.draw(renderer, matrix);
			icon.draw(renderer, matrix);
		};
	}
}
