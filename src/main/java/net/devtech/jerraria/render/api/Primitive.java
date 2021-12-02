package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.api.ShaderStage;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.render.internal.Shader;
import net.devtech.jerraria.render.internal.VAO;

public class Primitive extends ShaderStage {
	Shader shader;
	GlData ubo;

	void next() {
		((VAO)this.data).next();
	}

	void flush() {

	}
}
