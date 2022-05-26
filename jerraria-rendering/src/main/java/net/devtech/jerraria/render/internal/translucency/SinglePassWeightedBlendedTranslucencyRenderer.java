package net.devtech.jerraria.render.internal.translucency;

import static org.lwjgl.opengl.GL33.*;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

import java.util.List;
import java.util.Vector;

import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.translucency.TranslucencyRenderer;
import net.devtech.jerraria.render.api.translucency.TranslucencyStrategy;
import net.devtech.jerraria.render.api.translucency.Translucent;
import net.devtech.jerraria.render.internal.state.GLContextState;

public class SinglePassWeightedBlendedTranslucencyRenderer implements TranslucencyRenderer {
	final List<Translucent<?>> translucents = new Vector<>();
	int revealage; // doesn't need to get cleared
	int accum;

	public SinglePassWeightedBlendedTranslucencyRenderer() {
		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		int width = dims[2], height = dims[3];
		this.revealage = allocateTexture(width, height, GL_R32F); // doesn't need to get cleared
		this.accum = allocateTexture(width, height, GL_RGBA32F);
	}

	@Override
	public TranslucencyStrategy getStrategy() {
		return TranslucencyStrategy.SINGLE_PASS_WEIGHTED_BLENDED;
	}

	@Override
	public void registerInstance(Translucent<?> translucent) {
		this.translucents.add(translucent);
	}

	@Override
	public void draw() {
		GLContextState.DEPTH_TEST.set(true);
		GLContextState.DEPTH_FUNC.set(GL_LESS);
		GLContextState.DEPTH_MASK.set(false);
		GLContextState.BLEND.set(true);
		GLContextState.BLEND_STATE_IS[0].set(GL_ONE, GL_ONE);
		GLContextState.BLEND_STATE_IS[1].set(GL_ZERO, GL_ONE_MINUS_SRC_COLOR);
		GLContextState.BLEND_EQUATION.set(GL_FUNC_ADD);
		for(Translucent<?> translucent : this.translucents) {
			translucent.draw();
		}
	}

	@Override
	public void onFrameResize(int width, int height) {
		this.revealage = allocateTexture(width, height, GL_R32F); // doesn't need to get cleared
		this.accum = allocateTexture(width, height, GL_RGBA32F);
	}

	public static int allocateTexture(int width, int height, int storageType) {
		int tex = glGenTextures();
		int type = DataType.TEXTURE_2D.elementType;
		glBindTexture(type, tex);
		glTexStorage2D(type, 1, storageType, width, height);
		return tex;
	}
}
