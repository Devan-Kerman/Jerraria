package net.devtech.jerraria.render.api;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.internal.BareShader;

public class ShaderImpl implements Shader {
	public static final Map<Id, BareShader> SHADERS;
	static Primitive<?> activeVAO;

	static {
		SHADERS = BareShader.compileShaders(null, null, null);
	}

	final Id id;
	final List<GlValue.Type<?>> vertex, uniform;
	final List<ShaderStage> uniformStages, vertexStages;
	final Primitive<?> primitive;
	final ShaderStage stage;
	final BareShader shader;

	public ShaderImpl(Id id, List<GlValue.Type<?>> vertex, List<GlValue.Type<?>> uniforms) {
		this.id = id;
		this.vertex = vertex;
		this.uniform = uniforms;

		List<ShaderStage> vertexStages = new ArrayList<>(vertex.size());
		BareShader shader = null; // todo
		this.shader = shader;

		ShaderStage start = new End();
		for(int i = vertex.size() - 1; i >= 0; i--) {
			GlValue.Type<?> type = vertex.get(i);
			ShaderStage vertex_ = type.create();
			vertex_.next = start;
			vertex_.data = shader.vao;
			vertexStages.add(vertex_);
		}

		List<ShaderStage> uniformStages = new ArrayList<>(uniforms.size());
		ShaderStage next = this.primitive = new Primitive<>();
		for(int i = uniforms.size() - 1; i >= 0; i--) {
			GlValue.Type<?> type = uniforms.get(i);
			ShaderStage uniform = type.create();
			uniform.next = next;
			uniform.data = shader.uniforms;
			uniformStages.add(uniform);
			next = uniform;
		}

		this.uniformStages = uniformStages;
		this.vertexStages = vertexStages;
		this.stage = next;
	}

	@Override
	public ShaderStage start() {
		return this.stage;
	}

	@Override
	public Shader copy() {
		return new ShaderImpl(this.id, this.vertex, this.uniform);
	}
}
