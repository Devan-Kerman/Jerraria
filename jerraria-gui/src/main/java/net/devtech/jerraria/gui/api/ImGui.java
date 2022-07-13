package net.devtech.jerraria.gui.api;

import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import net.devtech.jerraria.gui.impl.ImGuiRendererImpl;
import net.devtech.jerraria.render.api.GlStateStack;
import net.devtech.jerraria.render.api.types.Tex;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
import org.jetbrains.annotations.Nullable;

/**
 * <br> Inspired by <a href="https://docs.unity3d.com/Manual/gui-Basics.html">Unity</a> & <a
 * href="https://github.com/ocornut/imgui">Dear ImGui</a>
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
	public final void render(TextRenderer<?> textRenderer, Mat current, float realWidth, float realHeight) {
		float scaleX = realWidth / this.getGuiScaleX();
		float scaleY = realHeight / this.getGuiScaleY();
		float max = Math.min(scaleX, scaleY);
		Mat mat = current.copy().scale(max, max);
		ImGuiRenderer renderer = new ImGuiRendererImpl(mat, textRenderer);
		float width = realWidth / max;
		float height = realHeight / max;
		AutoCentering centering = this.centering(width, height);
		SubdivisionStack stack = centering != null ? renderer.absolute((width - centering.desiredWidth)/2, (height - centering.desiredHeight)/2) : null;
		try(GlStateStack.builder().blend(true).blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA).apply().self) {
			this.render0(renderer, width, height);
			renderer.draw(shader -> {});
		} finally {
			if(stack != null) {
				stack.pop();
			}
		}
	}

	/**
	 * @param gui the gui renderer
	 * @param width the real width of the screen. For example if the user's screen's aspect ratio is greater than
	 *    {@link #aspectRatio()} the gui will be bounded by the screen's height, and you will have more space than
	 *    normal
	 * 	to render your GUI.
	 * @param height the real width of the screen.
	 */
	protected abstract void render0(ImGuiRenderer gui, float width, float height);

	/**
	 * Create a subdivision with absolute positioning at the center of the gui area.
	 */
	public SubdivisionStack center(
		ImGuiRenderer renderer, float width, float height, float desiredWidth, float desiredHeight) {
		return renderer.absolute((width - desiredWidth) / 2, (height - desiredHeight) / 2);
	}

	public record AutoCentering(float desiredWidth, float desiredHeight) {}

	/**
	 * Override this method to allow for automatic centering within the gui space
	 *
	 * <pre>{@code
	 * public AutoCentering centering(float width, float height) {
	 *     return new AutoCentering(100, 100); // offset the gui rendering such that a 100x square will be at the center.
	 * }
	 * }</pre>
	 * @param width the current gui width
	 * @see #render0(ImGuiRenderer, float, float)
	 * @see #defaultCentering()
	 * @return an object that states the desired drawing space
	 */
	@Nullable
	public AutoCentering centering(float width, float height) {
		return null;
	}

	public AutoCentering defaultCentering() {
		return new AutoCentering(this.getGuiScaleX(), this.getGuiScaleY());
	}

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

	public final float aspectRatio() {
		return this.getGuiScaleX() / this.getGuiScaleY();
	}
}
