package net.devtech.jerraria.render;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonElement;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.devtech.jerraria.render.api.base.DataType;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlShader;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.util.Identifier;

public abstract class CustomShader {
	public final IntList attributeLocations;
	public final VertexFormat format;
	public final int programId;
	public final Identifier id;
	public final List<String> samplers;
	@Nullable public final GlBlendState defaultBlend;

	ImmutableMap<String, GlUniform> loadedUniforms;
	List<GlUniform> vanillaUniforms;
	IntList uniformLocations;

	protected CustomShader(
		VertexFormat format, IntList attributeLocations, int programId, Identifier id, List<String> samplers) {
		this(format, attributeLocations, programId, id, samplers, null);
	}

	protected CustomShader(
		VertexFormat format,
		IntList attributeLocations,
		int programId,
		Identifier id,
		List<String> samplers,
		@Nullable GlBlendState blend) {

		this.samplers = List.copyOf(samplers);
		if(format.getAttributeNames().size() != attributeLocations.size()) {
			throw new IllegalArgumentException("Attribute location list must equal attributes list");
		}

		this.format = format;
		this.programId = programId;
		this.id = id;
		this.defaultBlend = blend;
		this.attributeLocations = IntLists.unmodifiable(new IntArrayList(attributeLocations));
	}

	protected abstract List<GlUniform> createVanillaUniforms(GlShader shader);

	public Map<String, GlUniform> getLoadedUniforms(GlShader shader) {
		this.initUniforms(shader);
		return this.loadedUniforms;
	}

	public List<GlUniform> getVanillaUniforms(GlShader shader) {
		this.initUniforms(shader);
		return this.vanillaUniforms;
	}

	public IntList getUniformLocations(GlShader shader) {
		this.initUniforms(shader);
		return this.uniformLocations;
	}

	public abstract void bind();

	/**
	 * Fired when vanilla sets the value of one of your uniforms
	 */
	public abstract void setVanillaOverwrite(GlUniform uniform);

	/**
	 * @return If one of your uniforms overwrites the passed vanilla one, return false
	 */
	public boolean doesOverwrite(GlUniform uniform) {
		return false;
	}

	public abstract void setSampler(String samplerName, int samplerIndex, int glId);

	@Nullable
	public static GlUniform createUniform(String name, DataType type, GlShader shader) {
		int dataType = dataType(type);
		if(dataType != -1) {
			return new GlUniform(name, dataType, type.elementCount, shader);
		} else {
			return null;
		}
	}

	/**
	 * [vanillacopy]
	 *
	 * @see GlUniform#getTypeIndex(String)
	 * @see Shader#addUniform(JsonElement)
	 */
	public static int dataType(DataType type) {
		int i;
		i = switch(type) {
			case F32, F32_VEC2, F32_VEC3, F32_VEC4 -> 4;
			case I32, I32_VEC2, I32_VEC3, I32_VEC4 -> 0;
			case MAT2 -> 8;
			case MAT3 -> 9;
			case MAT4 -> 10;
			default -> -1;
		};

		if(i == -1) {
			return -1;
		}

		int j = type.elementCount;
		int l = j > 1 && j <= 4 && i < 8 ? j - 1 : 0;
		return i + l;
	}

	void initUniforms(GlShader shader) {
		if(this.vanillaUniforms != null) {
			return;
		}
		synchronized(this) {
			if(this.vanillaUniforms != null) {
				return;
			}
			List<GlUniform> uniforms = List.copyOf(this.createVanillaUniforms(shader));
			IntList locations = new IntArrayList(uniforms.size());
			ImmutableMap.Builder<String, GlUniform> uniformMap = new ImmutableMap.Builder<>();
			for(GlUniform uniform : uniforms) {
				int location = uniform.getLocation();
				if(location == -1) {
					throw new IllegalArgumentException("GlUniform does not have location!");
				}
				locations.add(location);
				uniformMap.put(uniform.getName(), uniform);
			}

			this.vanillaUniforms = uniforms;
			this.uniformLocations = IntLists.unmodifiable(locations);
			this.loadedUniforms = uniformMap.build();
		}
	}
}
