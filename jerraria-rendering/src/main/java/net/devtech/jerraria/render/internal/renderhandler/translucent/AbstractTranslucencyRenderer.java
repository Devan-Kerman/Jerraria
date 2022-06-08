package net.devtech.jerraria.render.internal.renderhandler.translucent;

import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetIntegerv;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import net.devtech.jerraria.render.api.BuiltGlState;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.translucency.TranslucencyRenderer;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.renderhandler.InternalTranslucencyRenderer;
import net.devtech.jerraria.render.internal.renderhandler.OpaqueRenderHandler;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;

public abstract class AbstractTranslucencyRenderer extends OpaqueRenderHandler implements RenderHandler, InternalTranslucencyRenderer {
	public List<RenderCall> renderQueue = new ArrayList<>();
	public record RenderCall(Shader<?> shader, Consumer<Shader<?>> exec) {}

	public AbstractTranslucencyRenderer() {
		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		int width = dims[2], height = dims[3];
		this.frameSize(width, height);
	}

	public void clearRenderQueue() throws Exception {
		for(RenderCall call : this.renderQueue) {
			call.shader.close();
		}
		this.renderQueue.clear();
	}

	@Override
	public <S extends TranslucentShader<?>> S create(
		Id id, Copier<S> copier, Initializer<S> initializer) {
		return ShaderImpl.createShader(id,
			(o, s) -> copier.copy(o, s, this.type()),
			(u, c) -> initializer.create(u, c, this.type()),
			this
		);
	}

	@Override
	public BuiltGlState defaultGlState() {
		return this.type().defaultState;
	}

	@Override
	public void drawKeep(Shader<?> shader, BuiltGlState state) {
		this.renderQueue.add(new RenderCall(Shader.copy(shader, SCopy.PRESERVE_BOTH), s -> super.drawKeep(s, state)));
	}

	@Override
	public void drawInstancedKeep(Shader<?> shader, BuiltGlState state, int count) {
		this.renderQueue.add(new RenderCall(Shader.copy(shader, SCopy.PRESERVE_BOTH), s -> super.drawInstancedKeep(s, state, count)));
	}

	protected abstract TranslucentShaderType type();

	@Override
	public void renderStart() {}

	/*public static <S extends TranslucentShader<?>> void drawKeep(TranslucentPass<S> translucent) {
		BareShader shaderA = translucent.getPrimaryShader().getShader();
		shaderA.bindProgram();
		shaderA.drawKeep(translucent.getPrimaryShader().strategy.defaultState);
		if(translucent.getSecondPass() != null) {
			BareShader shaderB = translucent.getSecondPass().getShader();
			translucent.getSecondPass().strategy.defaultState.apply();
			shaderB.bindProgram();
			shaderA.drawKeep(translucent.getPrimaryShader().strategy.defaultState);
		}
	}

	public static <S extends TranslucentShader<?>> void drawInstancedKeep(TranslucentPass<S> translucent, int instances) {
		BareShader shaderA = translucent.getPrimaryShader().getShader();
		shaderA.bindProgram();
		shaderA.drawInstancedKeep(translucent.getPrimaryShader().strategy.defaultState, instances);
		if(translucent.getSecondPass() != null) {
			BareShader shaderB = translucent.getSecondPass().getShader();
			shaderB.bindProgram();
			shaderA.drawInstancedKeep(translucent.getSecondPass().strategy.defaultState, instances); // redraw with new shader
		}
	}*/
}
