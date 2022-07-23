package net.devtech.jerraria.mixin.self;

import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL20.glUniformMatrix2fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix3fv;
import static org.lwjgl.opengl.GL20.glUniformMatrix4fv;

import java.nio.FloatBuffer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.internal.Uniform;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

public class UniformMixin {
	@Mixin(targets = "net.devtech.jerraria.render.internal.Uniform$Matrix", remap = false)
	static abstract class MatrixMixin extends Uniform {
		protected MatrixMixin(DataType type, int location) {
			super(type, location);
		}

		@Shadow abstract void reset();
		@Shadow FloatBuffer buf;

		@Overwrite
		void upload() {
			this.reset();
			switch(this.type.elementCount) {
				case 4 -> RenderSystem.glUniformMatrix2(this.location, false, this.buf);
				case 9 -> RenderSystem.glUniformMatrix3(this.location, false, this.buf);
				case 16 -> RenderSystem.glUniformMatrix4(this.location, false, this.buf);
				default -> throw new UnsupportedOperationException("Unsupported matrix size " + this.type.elementCount);
			}
		}
	}

	@Mixin(targets = "net.devtech.jerraria.render.internal.Uniform$Sampler", remap = false)
	static abstract class SamplerMixin extends Uniform {
		@Shadow int textureUnit;
		@Shadow int textureId;

		protected SamplerMixin(DataType type, int location) {
			super(type, location);
		}

		@Overwrite
		void alwaysUpload() {
			RenderSystem.activeTexture(GL13.GL_TEXTURE0 + this.textureUnit);
			if(this.type.elementType == GL11.GL_TEXTURE_2D) {
				RenderSystem.bindTexture(textureId);
			} else {
				glBindTexture(this.type.elementType, textureId);
			}
		}
	}
}
