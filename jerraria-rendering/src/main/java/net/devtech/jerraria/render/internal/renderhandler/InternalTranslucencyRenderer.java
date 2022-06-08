package net.devtech.jerraria.render.internal.renderhandler;

import net.devtech.jerraria.render.api.translucency.TranslucencyRenderer;

public interface InternalTranslucencyRenderer extends TranslucencyRenderer {
	void renderStart();

	void renderResolve() throws Exception;

	void frameSize(int width, int height);
}
