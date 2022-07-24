package net.devtech.jerraria.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import net.devtech.jerraria.impl.render.CustomShaderImpl;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.ElementImpl;
import net.devtech.jerraria.render.internal.Uniform;
import net.devtech.jerraria.render.internal.UniformData;

import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;

public class MinecraftShaderLoader extends CustomShader {
	public static Shader createShader(MinecraftShader<?> shader) {
		return createShader(shader, MinecraftShaderLoader::new);
	}

	public static Shader createShader(MinecraftShader<?> shader, CustomMinecraftShaderInit loader) {
		ImmutableList<String> attributeNames = shader.format.getAttributeNames();
		IntArrayList locationsBuilder = new IntArrayList(attributeNames.size());
		BareShader bare = shader.getShader();
		for(String name : attributeNames) {
			ElementImpl element = (ElementImpl) bare.vao.getElement(name);
			locationsBuilder.add(element.location());
		}

		Map<String, GlData.Element> elements = bare.uniforms.elements;
		List<String> samplerIds = new ArrayList<>();
		elements.forEach((name, element) -> {
			if(element instanceof UniformData.StandardUniform e) {
				DataType type = e.type();
				if(type == DataType.TEXTURE_2D) {
					samplerIds.add(name);
				}
			}
		});
		samplerIds.sort(Comparator.comparing(k -> ((ElementImpl) elements.get(k)).location()));

		return CustomShaderImpl.createShader(loader.create(shader, locationsBuilder, samplerIds));
	}

	public interface CustomMinecraftShaderInit {
		CustomShader create(MinecraftShader<?> shader, IntList attributeLocations, List<String> samplers);
	}

	final MinecraftShader<?> shader;

	protected MinecraftShaderLoader(
		MinecraftShader<?> shader, IntList attributeLocations, List<String> samplers) {
		super(shader.format,
			attributeLocations,
			shader.getShader().id.glId,
			shader.getId().to(),
			samplers,
			shader.defaultBlendState()
		);
		this.shader = shader;
	}

	public MinecraftShader<?> getShader() {
		return this.shader;
	}

	@Override
	protected List<GlUniform> createVanillaUniforms(GlShader shader) {
		List<GlUniform> uniforms = new ArrayList<>();
		this.getShader().getShader().uniforms.elements.forEach((s, element) -> {
			if(element instanceof UniformData.StandardUniform e) {
				GlUniform uniform = createUniform(s, e.type(), shader);
				if(uniform != null) {
					uniform.setLocation(e.location());
					uniforms.add(uniform);
				}
			}
		});
		return uniforms;
	}

	@Override
	public void bind() {
		this.getShader().getShader().bindProgram();
		this.getShader().getShader().setupDraw(false);
	}

	@Override
	public void setVanillaOverwrite(GlUniform uniform) {
		Uniform managed = (Uniform) this.getShader().getShader().uniforms.element(uniform.getName());
		managed.reupload = false;
		managed.state.updateUniform(null, true);
	}

	@Override
	public boolean doesOverwrite(GlUniform uniform) {
		Uniform managed = (Uniform) this.getShader().getShader().uniforms.element(uniform.getName());
		return managed.reupload;
	}

	@Override
	public void setSampler(String samplerName, int samplerIndex, int glId) {
		this.getShader().getShader().uniforms.element(samplerName).i(glId);
	}
}
