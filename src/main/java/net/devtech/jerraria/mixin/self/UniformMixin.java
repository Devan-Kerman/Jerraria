package net.devtech.jerraria.mixin.self;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL13.glActiveTexture;

import java.nio.FloatBuffer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public class UniformMixin {
	@Mixin(targets = "net.devtech.jerraria.render.internal.Uniform$Matrix", remap = false)
	static class MatrixMixin {
		@Redirect(method = "upload()V",
			at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20;glUniformMatrix2fv(IZLjava/nio/FloatBuffer;)"
			                                    + "V"))
		static void mat2(int loc, boolean transpose, FloatBuffer buffer) {
			RenderSystem.glUniformMatrix2(loc, transpose, buffer);
		}

		@Redirect(method = "upload()V",
			at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20;glUniformMatrix3fv(IZLjava/nio/FloatBuffer;)"
			                                    + "V"))
		static void mat3(int loc, boolean transpose, FloatBuffer buffer) {
			RenderSystem.glUniformMatrix3(loc, transpose, buffer);
		}

		@Redirect(method = "upload()V",
			at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL20;glUniformMatrix4fv(IZLjava/nio/FloatBuffer;)"
			                                    + "V"))
		static void mat4(int loc, boolean transpose, FloatBuffer buffer) {
			RenderSystem.glUniformMatrix4(loc, transpose, buffer);
		}
	}

	@Mixin(targets = "net.devtech.jerraria.render.internal.Uniform$Sampler", remap = false)
	static class SamplerMixin {
		@Redirect(method = "alwaysUpload()V",
			at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL13;glActiveTexture(I)V"))
		void alwaysUpload(int active) {
			RenderSystem.activeTexture(active);
		}

		@Redirect(method = "alwaysUpload()V",
			at = @At(value = "INVOKE", target = "Lorg/lwjgl/opengl/GL11;glBindTexture(II)V"))
		void alwaysUpload(int target, int id) {
			if(target == GL11.GL_TEXTURE_2D) {
				RenderSystem.bindTexture(id);
			} else {
				glBindTexture(target, id);
			}
		}
	}
}
