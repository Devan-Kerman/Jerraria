package net.devtech.jerraria.gui.api;

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

	public final Mat createGuiMatrix(Mat2x3f current, float realScaleX, float realScaleY) {
		float scaleX = realScaleX / this.getGuiScaleX();
		float scaleY = realScaleY / this.getGuiScaleY();
		float max = Math.min(scaleX, scaleY);
		return current.copy().scale(max, max);
	}

	protected abstract void render0(ImGuiRenderer gui);

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
