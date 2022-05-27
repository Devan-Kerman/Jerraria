package net.devtech.jerraria.render.api;

import static org.lwjgl.opengl.GL11.GL_ONE;
import static org.lwjgl.opengl.GL11.GL_ZERO;

import net.devtech.jerraria.render.internal.state.GLContextState;

record BuiltGlStateImpl(GLStateBuilder copy) implements BuiltGlState {
	@Override
	public void apply() {
		GLContextState.DEPTH_FUNC.set(this.copy.depthFunc);
		GLContextState.BLEND_EQUATION.set(this.copy.blendEquation);
		GLContextState.DEPTH_MASK.set(this.copy.depthMask);
		GLContextState.DEPTH_TEST.set(this.copy.depthTest);
		GLContextState.BLEND.set(this.copy.blend);
		if(this.copy.srcStates != null) {
			if(this.copy.all) {
				GLContextState.blendFunc(this.copy.srcStates[0], this.copy.dstStates[0]);
			} else {
				if(this.copy.srcStates.length != this.copy.dstStates.length) {
					throw new UnsupportedOperationException("blend func lengths are different!");
				}
				for(int i = 0; i < this.copy.srcStates.length; i++) {
					GLContextState.BLEND_STATE_IS[i].set(this.copy.srcStates[i], this.copy.dstStates[i]);
				}
			}
		} else {
			GLContextState.blendFunc(GL_ONE, GL_ZERO);
		}
	}

	@Override
	public GLStateBuilder copyToBuilder() {
		return new GLStateBuilder(this.copy);
	}
}
