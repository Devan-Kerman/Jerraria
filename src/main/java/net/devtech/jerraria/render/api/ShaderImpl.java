package net.devtech.jerraria.render.api;

import java.util.List;

public class ShaderImpl {
	final List<GlValue.Type<?>> vertex, uniform;

	public ShaderImpl(List<GlValue.Type<?>> vertex, List<GlValue.Type<?>> uniform) {
		this.vertex = vertex;
		this.uniform = uniform;
	}
}
