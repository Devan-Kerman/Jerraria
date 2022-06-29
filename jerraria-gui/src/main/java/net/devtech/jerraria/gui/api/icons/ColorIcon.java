package net.devtech.jerraria.gui.api.icons;

import net.devtech.jerraria.gui.api.LayeredBatchedRenderer;
import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.batch.BasicShaderKey;
import net.devtech.jerraria.render.api.batch.ShaderKey;
import net.devtech.jerraria.render.api.element.AutoStrat;

public record ColorIcon(int argb, ShaderKey<SolidColorShader> key) implements Icon {
	public ColorIcon(int argb) {
		this(argb, (argb & 0xFF000000) == 0xFF000000 ? SOLID_KEY : TRANSLUCENT_KEY);
	}

	static final BasicShaderKey<SolidColorShader> SOLID_KEY = SolidColorShader.KEYS.getFor(AutoStrat.QUADS);
	public static final ShaderKey<SolidColorShader> TRANSLUCENT_KEY = SOLID_KEY.withState(BuiltGlState.builder().depthMask(false));

	@Override
	public void draw(LayeredBatchedRenderer renderer) {
		SolidColorShader batch = renderer.getBatch(key);
		batch.rect(renderer.mat(), 0, 0, 1, 1, this.argb);
	}

	@Override
	public float aspectRatio() {
		return 1;
	}
}
