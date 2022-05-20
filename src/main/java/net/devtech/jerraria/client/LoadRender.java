package net.devtech.jerraria.client;

import java.util.List;
import java.util.Vector;

import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.shaders.SolidColorShader;

public class LoadRender {
	final LoadRender parent;
	List<LoadRender> children = new Vector<>();

	String titleText;
	int completed, taskSize; // make some things atomic

	public static LoadRender create(String title, int size) {
		return new LoadRender(null, title, size);
	}

	protected LoadRender(LoadRender parent, String titleText, int taskSize) {
		this.parent = parent;
		this.titleText = titleText;
		this.taskSize = taskSize;
	}

	public static final int[] RAINBOW = {0xFF0000, 0xFF8800, 0xFFFF00, 0x88FF00, 0x00FF00, 0x00FF88, 0x00FFFF, 0x0088FF, 0x0000FF};

	public void render(Matrix3f mat, SolidColorShader box, ColoredTextureShader text, float width, float offX, float offY) {
		box.drawRect(mat, offX + .05f, offY + .05f, width-.1f, .9f, 0xFFAAAAAA);

		float ratio = this.completed / (float) this.taskSize;
		float realWidth = width - .2f;
		float barWidth = ratio * realWidth;

		box.drawRect(mat, offX + .1f, offY + .1f, width - .2f, .8f, 0xFFAABBBB);
		int hashCode = this.hashCode();
		box.drawRect(mat, offX + .1f, offY + .1f, barWidth, .8f, 0xFF000000 | RAINBOW[hashCode % RAINBOW.length]);

		try(var mov = mat.copy().offset(offX+.1f, offY+.1f).scale(.5f, .5f)) {
			renderText(
				mov,
				text,
				String.format(this.titleText, this.completed, this.taskSize),
				0xFF000000 | RAINBOW[(hashCode + 2) % RAINBOW.length],
				0,
				0
			);
		}

		float currentOffset = 0;
		List<LoadRender> copy = new Vector<>(this.children);
		if(!copy.isEmpty()) {
			copy.removeIf(l -> l.completed >= l.taskSize);
		}
		float increment = width / copy.size();
		for(LoadRender child : copy) {
			child.render(mat, box, text, increment, currentOffset, offY + 1);
			currentOffset += increment;
		}
	}

	public LoadRender substage(String titleText, int initialTaskSize) {
		LoadRender stage = new LoadRender(this, titleText, initialTaskSize);
		this.children.add(stage);
		return stage;
	}

	public void complete(int amount) {
		this.completed += amount;
	}

	public LoadRender getParent() {
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

	public void setToComplete() {
		this.completed = this.taskSize;
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

			instance.vert().vec3f(mat, xSPos, offY, 1).vec2f(x*uInc, y*vInc).argb(rgb);
			instance.vert().vec3f(mat, xSPos+1, offY, 1).vec2f(x*uInc+uInc, y*vInc).argb(rgb);
			instance.vert().vec3f(mat, xSPos, offY + 1, 1).vec2f(x*uInc, y*vInc + vInc).argb(rgb);

			instance.vert().vec3f(mat, xSPos+1, offY, 1).vec2f(x*uInc+uInc, y*vInc).argb(rgb);
			instance.vert().vec3f(mat, xSPos, offY + 1, 1).vec2f(x*uInc, y*vInc + vInc).argb(rgb);
			instance.vert().vec3f(mat, xSPos+1, offY + 1, 1).vec2f(x*uInc + uInc, y*vInc + vInc).argb(rgb);
		}
	}
}
