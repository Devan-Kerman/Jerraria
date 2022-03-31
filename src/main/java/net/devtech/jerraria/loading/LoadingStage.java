package net.devtech.jerraria.loading;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import net.devtech.jerraria.render.ClientRenderContext;
import net.devtech.jerraria.render.math.Matrix3f;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.shaders.SolidColorShader;

public class LoadingStage {
	final LoadingStage parent;
	List<LoadingStage> children = List.of();

	String titleText;
	int completed, taskSize;

	public static LoadingStage create(String title, int size) {
		return new LoadingStage(null, title, size);
	}

	protected LoadingStage(LoadingStage parent, String titleText, int taskSize) {
		this.parent = parent;
		this.titleText = titleText;
		this.taskSize = taskSize;
	}

	public static final int[] RAINBOW = {0xFF0000, 0xFF8800, 0xFFFF00, 0x88FF00, 0x00FF00, 0x00FF88, 0x00FFFF, 0x0088FF, 0x0000FF};

	public void render(Matrix3f mat, SolidColorShader box, ColoredTextureShader text, float width, float offX, float offY) {
		box.drawRect(mat, offX + .05f, offY + .05f, width-.1f, .9f, 0xAAAAAA);

		float ratio = this.completed / (float) this.taskSize;
		float realWidth = width - .2f;
		float barWidth = ratio * realWidth;

		box.drawRect(mat, offX + .1f, offY + .1f, width - .2f, .8f, 0xFFFFFF);
		int hashCode = this.hashCode();
		box.drawRect(mat, offX + .1f, offY + .1f, barWidth, .8f, RAINBOW[hashCode % RAINBOW.length]);

		renderText(mat, text, String.format(this.titleText, this.completed, this.taskSize), RAINBOW[(hashCode+3) % RAINBOW.length], offX, offY);

		float currentOffset = 0;
		float increment = width / this.children.size();
		for(LoadingStage child : this.children) {
			child.render(mat, box, text, increment, currentOffset, offY + 1);
			currentOffset += increment;
		}
	}

	public LoadingStage allocateSubstage(String titleText, int initialTaskSize) {
		if(this.children.isEmpty()) {
			this.children = new ArrayList<>();
		}
		LoadingStage stage = new LoadingStage(this, titleText, initialTaskSize);
		this.children.add(stage);
		return stage;
	}

	public void complete(int amount) {
		this.completed += amount;
	}

	public LoadingStage getParent() {
		return this.parent;
	}

	public String getTitleText() {
		return this.titleText;
	}

	public void setTitleText(String titleText) {
		this.titleText = titleText;
	}

	public int getTaskSize() {
		return this.taskSize;
	}

	public void setTaskSize(int taskSize) {
		this.taskSize = taskSize;
	}

	// y = 1/8
	// x = height / width*8

	/**
	 * this code is terrible, cope
	 * loading screen text is rendered on an 8 line grid
	 */
	public static void renderText(Matrix3f mat, ColoredTextureShader instance, String text, int rgb, float offX, float offY) {
		for(int i = 0; i < text.length(); i++) {
			int index = text.charAt(i) - ' ';
			int x = index % 16, y = index / 16;

			float xSPos = 2*i/3f + offX;
			float uInc = 7/128F, vInc = 8/64F;

			instance.vert().vec3f(mat, xSPos, offY, 1).vec2f(x*uInc, y*vInc).rgb(rgb);
			instance.vert().vec3f(mat, xSPos+1, offY, 1).vec2f(x*uInc+uInc, y*vInc).rgb(rgb);
			instance.vert().vec3f(mat, xSPos, offY + 1, 1).vec2f(x*uInc, y*vInc + vInc).rgb(rgb);

			instance.vert().vec3f(mat, xSPos+1, offY, 1).vec2f(x*uInc+uInc, y*vInc).rgb(rgb);
			instance.vert().vec3f(mat, xSPos, offY + 1, 1).vec2f(x*uInc, y*vInc + vInc).rgb(rgb);
			instance.vert().vec3f(mat, xSPos+1, offY + 1, 1).vec2f(x*uInc + uInc, y*vInc + vInc).rgb(rgb);
		}
	}
}
