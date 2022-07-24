package net.devtech.jerraria.impl.render;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import net.devtech.jerraria.mixin.impl.ShaderAccess;
import net.devtech.jerraria.render.CustomShader;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.gl.GlUniform;
import net.minecraft.client.render.Shader;
import net.minecraft.client.texture.AbstractTexture;

public final class CustomShaderImpl extends Shader implements ShaderExt {
	static final ThreadLocal<CustomShader> SHADER_THREAD_LOCAL = new ThreadLocal<>();
	public static Shader createShader(CustomShader shader) {
		SHADER_THREAD_LOCAL.set(shader);
		try {
			return new CustomShaderImpl(shader);
		} catch(IOException e) {
			throw Validate.rethrow(e);
		} finally {
			SHADER_THREAD_LOCAL.set(null);
		}
	}

	final CustomShader shader;
	private CustomShaderImpl(CustomShader shader) throws IOException {
		super(null, shader.id.toString(), shader.format);
		this.shader = shader;
	}

	@Override
	public void markDirty(GlUniform uniform) {
		Objects.requireNonNullElseGet(this.shader, SHADER_THREAD_LOCAL::get).setVanillaOverwrite(uniform);
	}

	void init(CustomShader shader) {
		ShaderAccess access = (ShaderAccess) (Object) this;
		access.setSamplerNames(shader.samplers);
		access.setUniforms(shader.getVanillaUniforms(this));
		access.setLoadedUniforms(shader.getLoadedUniforms(this));
		access.setLoadedUniformIds(shader.getUniformLocations(this));
		access.setBlendState(shader.defaultBlend);
		access.setLoadedAttributeIds(shader.attributeLocations);
		access.setAttributeNames(shader.format.getAttributeNames());
		access.setProgramId(shader.programId);
	}

	boolean init;

	@Nullable
	@Override
	public GlUniform getUniform(String name) {
		if(!this.init) {
			this.init(SHADER_THREAD_LOCAL.get());
			this.init = true;
		}
		return super.getUniform(name);
	}

	@Override
	public void close() {
		throw new UnsupportedOperationException();
	}

	@Override
	public void bind() {
		// [vanillacopy]
		RenderSystem.assertOnRenderThread();
		ShaderAccess access = (ShaderAccess) (Object) this;
		access.setDirty(false);
		GlBlendState state = access.getBlendState();
		if(state != null) {
			state.enable();
		}

		List<String> names = access.getSamplerNames();
		int active = GlStateManager._getActiveTexture();
		for(int i = 0; i < names.size(); i++) {
			String name = names.get(i);
			Object object = access.getSamplers().get(name);
			if(object != null) {
				int glId = -1;
				if(object instanceof Framebuffer) {
					glId = ((Framebuffer) object).getColorAttachment();
				} else if(object instanceof AbstractTexture) {
					glId = ((AbstractTexture) object).getGlId();
				} else if(object instanceof Integer) {
					glId = (Integer) object;
				}
				this.shader.setSampler(name, i, glId);
			}
		}
		GlStateManager._activeTexture(active);
		this.shader.bind();
		for(GlUniform uniform : access.getUniforms()) {
			if(!this.shader.doesOverwrite(uniform)) {
				uniform.upload();
			}
		}
	}
}
