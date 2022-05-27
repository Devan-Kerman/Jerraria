package net.devtech.jerraria.render.internal.renderhandler.translucent;

import static org.lwjgl.opengl.GL11.GL_COLOR;
import static org.lwjgl.opengl.GL30.glClearBufferfv;

import java.util.function.BiConsumer;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.translucency.TranslucentInternal;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.FragOutput;
import net.devtech.jerraria.util.Id;

public class DoublePassWeightedRenderer extends WeightedTranslucentRenderer {
	@Override
	public <S extends TranslucentShader<?>> S create(
		Id id, Copier<S> copier, Initializer<S> initializer) {
		S shader = ShaderImpl.createShader(
			id,
			(o, s) -> copier.copy(o, s, TranslucentShaderType.DOUBLE_PASS_A),
			(i, u, c) -> initializer.create(i, u, c, TranslucentShaderType.DOUBLE_PASS_A),
			this
		);
		TranslucentInternal.setSecondPass(shader, ShaderImpl.createShader(
			id,
			(o, s) -> copier.copy(o, s, TranslucentShaderType.DOUBLE_PASS_B),
			(i, u, c) -> initializer.create(i, u, c, TranslucentShaderType.DOUBLE_PASS_B),
			this
		));
		return shader;
	}

	@Override
	public BuiltGlState defaultGlState() {
		return TranslucentShaderType.DOUBLE_PASS_A.defaultState;
	}

	@Override
	public void drawKeep(Shader<?> shader, BuiltGlState state) {
		this.draw(BareShader::drawKeep, shader, state);
	}

	@Override
	public void drawInstancedKeep(Shader<?> shader, BuiltGlState state, int count) {
		this.draw((b, s) -> b.drawInstancedKeep(s, count), shader, state);
	}

	@Override
	protected TranslucentShaderType type() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void refreshBuffers(Shader<?> shader, int count) {
		if(count == 0) {
			FragOutput outputs = shader.getShader().outputs;
			outputs.bind();
			glClearBufferfv(GL_COLOR, 0, ZEROS);
		} else if(count == 1) {
			FragOutput outputs = shader.getShader().outputs;
			outputs.bind();
			glClearBufferfv(GL_COLOR, 0, ONES);
		}
	}

	@Override
	protected void initialize(TranslucentShader<?> lucent) {
		if(lucent.doublePassWeightedA != null) {
			lucent.doublePassWeightedA.accum.tex(this.accum);
		} else {
			lucent.doublePassWeightedB.reveal.tex(this.revealage);
		}
	}

	private void draw(BiConsumer<BareShader, BuiltGlState> built, Shader<?> shader, BuiltGlState state) {
		Shader<?> copy = Shader.copy(shader, SCopy.PRESERVE_BOTH);
		this.renderQueue.add(new RenderCall(copy, s -> {
			BareShader bare = s.getShader();
			bare.bindProgram();
			built.accept(bare, state);
		}));
		TranslucentShader<?> secondPass = TranslucentInternal.getSecondPass((TranslucentShader<?>) shader);
		this.renderQueue.add(new RenderCall(secondPass, s -> {
			s.getShader().bindProgram();
			// use copied vertex data
			built.accept(copy.getShader(), secondPass.type.defaultState);
		}));
	}
}
