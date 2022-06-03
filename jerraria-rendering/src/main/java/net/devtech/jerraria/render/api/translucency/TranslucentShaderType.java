package net.devtech.jerraria.render.api.translucency;

import static org.lwjgl.opengl.GL14.GL_FUNC_ADD;
import static org.lwjgl.opengl.GL42.GL_ONE;
import static org.lwjgl.opengl.GL42.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL42.GL_ONE_MINUS_SRC_COLOR;
import static org.lwjgl.opengl.GL42.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL42.GL_ZERO;

import java.util.function.Supplier;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.internal.renderhandler.TranslucencyStrategy;

public enum TranslucentShaderType {
	LINKED_LIST(
		"430",
		TranslucencyStrategy.LINKED_LIST,
		GLStateBuilder
			.builder()
			.depthTest(true)
			.depthMask(false)
			.blend(true)
			.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
			.build()
	),
	SINGLE_PASS("330",
		TranslucencyStrategy.SINGLE_PASS_WEIGHTED_BLENDED,
		GLStateBuilder
			.builder()
			.depthTest(true)
			.depthMask(false)
			.blend(true)
			.srcBlendFuncs(GL_ONE, GL_ZERO)
			.dstBlendFuncs(GL_ONE, GL_ONE_MINUS_SRC_COLOR)
			.blendEquation(GL_FUNC_ADD)
			.build()
	),
	DOUBLE_PASS_A("330",
		TranslucencyStrategy.DOUBLE_PASS_WEIGHTED_BLENDED,
		GLStateBuilder.builder().depthTest(true).depthMask(false).blend(true).blendFunc(GL_ONE, GL_ONE).build()
	),
	DOUBLE_PASS_B("330",
		TranslucencyStrategy.DOUBLE_PASS_WEIGHTED_BLENDED,
		GLStateBuilder
			.builder()
			.depthTest(true)
			.depthMask(false)
			.blend(true)
			.blendFunc(GL_ZERO, GL_ONE_MINUS_SRC_COLOR)
			.build()
	);

	// todo immediate renderer

	public final BuiltGlState defaultState;
	final String glslVers;
	final TranslucencyStrategy strategy;

	TranslucentShaderType(
		String vers, TranslucencyStrategy strategy, BuiltGlState state) {
		this.glslVers = vers;
		this.strategy = strategy;
		this.defaultState = state;
	}

	public <T> T calcIf(
		TranslucentShaderType list, Supplier<T> aNew) {
		return this == list ? aNew.get() : null;
	}
}
