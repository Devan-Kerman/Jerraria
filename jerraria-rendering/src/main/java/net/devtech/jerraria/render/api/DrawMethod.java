package net.devtech.jerraria.render.api;

import java.util.List;

import org.lwjgl.opengl.GL11;

/**
 * A native opengl drawing method
 */
public enum DrawMethod {
	TRIANGLE(GL11.GL_TRIANGLES, 3, 3),
	LINES(GL11.GL_LINES, 2, 2),
	LINE_STRIP(GL11.GL_LINE_STRIP, 1, 2),
	LINE_LOOP(GL11.GL_LINE_LOOP, 1, 2),
	TRIANGLE_STRIP(GL11.GL_TRIANGLE_STRIP, 1, 3);

	public static final List<DrawMethod> VALUES = List.of(DrawMethod.values());

	public final int glId;
	public final int vertexCount;
	public final int minimumVertices;

	DrawMethod(int id, int count, int vertices) {
		this.glId = id;
		this.vertexCount = count;
		this.minimumVertices = vertices;
	}
}
