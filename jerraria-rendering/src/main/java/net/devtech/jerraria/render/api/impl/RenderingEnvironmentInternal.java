package net.devtech.jerraria.render.api.impl;


import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;

public class RenderingEnvironmentInternal {
	public static Thread renderThread_;
	public static BuiltGlState defaultState_ = GLStateBuilder.builder().build();
}
