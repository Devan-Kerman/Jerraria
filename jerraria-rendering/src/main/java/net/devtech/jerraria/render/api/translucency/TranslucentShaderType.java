package net.devtech.jerraria.render.api.translucency;

import java.util.function.Supplier;

import net.devtech.jerraria.render.api.GlValue;

public enum TranslucentShaderType {
	LINKED_LIST("430"), SINGLE_PASS("330"), DOUBLE_PASS_A("330"), DOUBLE_PASS_B("330");

	final String glslVers;

	TranslucentShaderType(String vers) {this.glslVers = vers;}

	public <T> T calcIf(
		TranslucentShaderType list, Supplier<T> aNew) {
		return this == list ? aNew.get() : null;
	}
}
