package net.devtech.jerraria.render;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonElement;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import it.unimi.dsi.fastutil.ints.IntLists;
import net.devtech.jerraria.mixin.impl.ShaderAccess;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.internal.BareShader;
import net.devtech.jerraria.render.internal.ElementImpl;
import net.devtech.jerraria.render.internal.Uniform;
import net.devtech.jerraria.render.internal.UniformData;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.texture.AbstractTexture;

public class JerrariaShader extends Shader implements ShaderExt {
	boolean inited;
	final MinecraftShader<?> shader;
	static final ThreadLocal<MinecraftShader<?>> SHADER_THREAD_LOCAL = new ThreadLocal<>();

	public static Shader create(MinecraftShader<?> shader) {
		try {
			SHADER_THREAD_LOCAL.set(shader);
			return new JerrariaShader(shader);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		} finally {
			SHADER_THREAD_LOCAL.set(null);
		}
	}

	public JerrariaShader(MinecraftShader<?> shader) throws IOException {
		super(null, shader.getId().toString(), shader.format);
		this.shader = shader;
	}

	public void init() { // allows early init
		ImmutableList<String> attributeNames = this.getFormat().getAttributeNames();
		IntArrayList locationsBuilder = new IntArrayList(attributeNames.size());
		MinecraftShader<?> current = SHADER_THREAD_LOCAL.get();
		BareShader bare = current.getShader();
		for(String name : attributeNames) {
			ElementImpl element = (ElementImpl) bare.vao.getElement(name);
			locationsBuilder.add(element.location());
		}
		IntList attributeIds = IntLists.unmodifiable(locationsBuilder);
		Map<String, GlData.Element> elements = bare.uniforms.elements;
		List<String> samplerIds = new ArrayList<>();
		List<GlUniform> uniforms = new ArrayList<>();
		Map<String, GlUniform> loaded = new HashMap<>();
		IntList uniformIds = new IntArrayList();
		elements.forEach((name, element) -> {
			if(element instanceof UniformData.StandardUniform e) {
				DataType type = e.type();
				if(type == DataType.TEXTURE_2D) {
					samplerIds.add(name);
				} else {
					int dataType = JerrariaShader.dataType(type);
					if(dataType != -1) {
						JerrariaShader.UniformUniform uniform = this.new UniformUniform(name, dataType, type);
						uniforms.add(uniform);
						loaded.put(name, uniform);
						uniformIds.add(e.location());
						uniform.setLocation(e.location());
					}
				}
			}
		});

		samplerIds.sort(Comparator.comparing(k -> ((ElementImpl) elements.get(k)).location()));

		ShaderAccess access = (ShaderAccess) this;
		access.setSamplerNames(samplerIds);
		access.setUniforms(uniforms);
		access.setLoadedUniforms(loaded);
		access.setLoadedUniformIds(uniformIds);
		access.setBlendState(current.defaultBlendState());
		access.setLoadedAttributeIds(attributeIds);
		access.setAttributeNames(attributeNames);
		access.setProgramId(bare.id.glId);
	}

	@Nullable
	@Override
	public GlUniform getUniform(String name) {
		if(!this.inited) {
			this.init();
			this.inited = true;
		}
		return super.getUniform(name);
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void bind() {
		RenderSystem.assertOnRenderThread();
		ShaderAccess access = (ShaderAccess) this;
		access.setDirty(false);
		GlBlendState state = access.getBlendState();
		if(state != null) {
			state.enable();
		}

		List<String> names = access.getSamplerNames();
		int active = GlStateManager._getActiveTexture();
		BareShader bare = this.shader.getShader();
		UniformData uniforms = bare.uniforms;
		for(String name : names) {
			Object object = access.getSamplers().get(name);
			if(object != null) {
				int l = -1;
				if(object instanceof Framebuffer) {
					l = ((Framebuffer) object).getColorAttachment();
				} else if(object instanceof AbstractTexture) {
					l = ((AbstractTexture) object).getGlId();
				} else if(object instanceof Integer) {
					l = (Integer) object;
				}
				uniforms.element(name).i(l);
			}
		}
		GlStateManager._activeTexture(active);
		bare.bindProgram();
		bare.setupDraw(false);

		for(GlUniform uniform : access.getUniforms()) {
			uniform.upload();
		}
	}

	@Override
	public void markDirty(String uniformName) {
		Uniform managed = (Uniform) Objects.requireNonNullElseGet(this.shader, SHADER_THREAD_LOCAL::get).getShader().uniforms.element(uniformName);
		managed.reupload = false;
		managed.state.updateUniform(null, true);
	}

	public class UniformUniform extends GlUniform {
		public UniformUniform(String name, int dataType, DataType type) {
			super(name, dataType, type.elementCount, JerrariaShader.this);
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
}
