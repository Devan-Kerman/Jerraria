package net.devtech.jerraria.render.api.translucency;

public interface TranslucencyRenderer {
	TranslucencyStrategy getStrategy();

	void registerInstance(Translucent<?> translucent);

	void draw();

	void onFrameResize(int width, int height);
}
