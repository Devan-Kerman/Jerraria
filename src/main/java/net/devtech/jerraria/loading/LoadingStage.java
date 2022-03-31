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
	String taskName;
	int completed, taskSize;

	protected LoadingStage(LoadingStage parent, String titleText, int taskSize) {
		this.parent = parent;
		this.titleText = titleText;
		this.taskSize = taskSize;
	}

	public void render(SolidColorShader box, ColoredTextureShader text, int width, int offX, int offY) {

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

	public String getTaskName() {
		return this.taskName;
	}

	public void setTaskName(String taskName) {
		this.taskName = taskName;
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
	public static void renderText(Matrix3f mat, ColoredTextureShader instance, String text) {
		for(int i = 0; i < text.length(); i++) {
			int index = text.charAt(i) - ' ';
			int x = index % 16, y = index / 16;

			float xSPos = 2*i/3f;
			float uInc = 7/128F, vInc = 8/64F;

			instance.vert().vec3f(mat, xSPos, 0, 1).vec2f(x*uInc, y*vInc).rgb(0xFFFFFF);
			instance.vert().vec3f(mat, xSPos+1, 0, 1).vec2f(x*uInc+uInc, y*vInc).rgb(0xFFFFFF);
			instance.vert().vec3f(mat, xSPos, 1, 1).vec2f(x*uInc, y*vInc + vInc).rgb(0xFFFFFF);

			instance.vert().vec3f(mat, xSPos+1, 0, 1).vec2f(x*uInc+uInc, y*vInc).rgb(0xFFFFFF);
			instance.vert().vec3f(mat, xSPos, 1, 1).vec2f(x*uInc, y*vInc + vInc).rgb(0xFFFFFF);
			instance.vert().vec3f(mat, xSPos+1, 1, 1).vec2f(x*uInc + uInc, y*vInc + vInc).rgb(0xFFFFFF);
		}
	}
}
