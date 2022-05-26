package net.devtech.jerraria.render.api.translucency;

import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.util.Id;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;

public final class Translucent<S extends TranslucentShader<?>> {
	private final S a, b;

	Translucent(S a, S b) {
		this.a = a;
		this.b = b;
	}

	public static <S extends TranslucentShader<?>> Translucent<S> create(
		Id id,
		Copier<S> copier,
		Initializer<S> initializer,
		TranslucencyStrategy strat) {

		S a, b;
		if(strat == TranslucencyStrategy.DOUBLE_PASS_WEIGHTED_BLENDED) {
			a = Shader.createShader(id,
				(o, s) -> copier.copy(o, s, TranslucentShaderType.DOUBLE_PASS_A),
				(i, u, c) -> initializer.create(i, u, c, TranslucentShaderType.DOUBLE_PASS_A)
			);
			b = Shader.createShader(id,
				(o, s) -> copier.copy(o, s, TranslucentShaderType.DOUBLE_PASS_B),
				(i, u, c) -> initializer.create(i, u, c, TranslucentShaderType.DOUBLE_PASS_B)
			);
		} else {
			a = Shader.createShader(id,
				(o, s) -> copier.copy(o, s, strat.shader),
				(i, u, c) -> initializer.create(i, u, c, strat.shader)
			);
			b = null;
		}

		return new Translucent<>(a, b);
	}

	/**
	 * @see TranslucencyRenderer
	 */
	@ApiStatus.Internal
	public void draw() {
		BareShader shaderA = this.a.getShader();
		shaderA.bindProgram();
		shaderA.draw();
		if(this.b != null) {
			BareShader shaderB = this.b.getShader();
			shaderB.bindProgram();
			shaderA.draw(); // redraw with new shader
		}
	}

	/**
	 * @see TranslucencyRenderer
	 */
	@ApiStatus.Internal
	public void drawInstanced(int instances) {
		BareShader shaderA = this.a.getShader();
		shaderA.bindProgram();
		shaderA.drawInstanced(instances);
		if(this.b != null) {
			BareShader shaderB = this.b.getShader();
			shaderB.bindProgram();
			shaderA.drawInstanced(instances); // redraw with new shader
		}
	}

	/**
	 * If the translucency method is a multi-pass algorithm, then the primary shader stores the VAO and Uniform Data.
	 *
	 * <b>DO NOT CALL {@link Shader#render()} on this instance
	 */
	public S getPrimaryShader() {
		return this.a;
	}

	/**
	 * Secondary pass shader, this is mostly useless, and you generally shouldn't touch this
	 */
	@Nullable
	public S getSecondPass() {
		return this.b;
	}

	public interface Copier<T extends Shader<?>> {
		T copy(T old, SCopy method, TranslucentShaderType type);
	}

	public interface Initializer<T extends Shader<?>> {
		T create(Id id, VFBuilder<End> builder, Object context, TranslucentShaderType type);
	}
}
