package net.devtech.jerraria.gui.api;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import net.devtech.jerraria.gui.impl.ImGuiRendererImpl;
import net.devtech.jerraria.render.api.GlStateStack;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;

/**
 * <br> Inspired by <a href="https://docs.unity3d.com/Manual/gui-Basics.html">Unity</a> & <a href="https://github.com/ocornut/imgui">Dear ImGui</a>
 */
public abstract class ImGui {
	protected float guiScaleX, guiScaleY;

	/**
	 * @see #getGuiScaleX()
	 */
	public ImGui(float guiScaleX, float guiScaleY) {
		this.guiScaleX = guiScaleX;
		this.guiScaleY = guiScaleY;
	}

	/**
	 * @param realWidth the width of the area to render within the `current` matrix's space
	 */
	public void render(Mat2x3f current, float realWidth, float realHeight) {
		float scaleX = realWidth / this.getGuiScaleX();
		float scaleY = realHeight / this.getGuiScaleY();
		float max = Math.min(scaleX, scaleY);
		Mat mat = current.copy().scale(max, max);
		ImGuiRenderer renderer = new ImGuiRendererImpl(mat);
		try(GlStateStack.builder().blend(true).blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).depthTest(true).apply().self) {
			this.render0(renderer, realWidth/max, realHeight/max);
			renderer.draw(shader -> {});
		}
	}

	protected abstract void render0(ImGuiRenderer gui, float width, float height);

	/**
	 * @return The minimum guaranteed width of the screen, for example if the user shrinks the window to be too thin,
	 * 	then the GUI is scaled down (via matrix) to ensure this width can always fit.
	 */
	public float getGuiScaleX() {
		return this.guiScaleX;
	}

	/**
	 * @return The minimum guaranteed height of the screen.
	 * @see #getGuiScaleX()
	 */
	public float getGuiScaleY() {
		return this.guiScaleY;
	}
}
