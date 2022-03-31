package net.devtech.jerraria.render.api;

import org.lwjgl.opengl.GL11;

public enum Primitive {
	TRIANGLE(GL11.GL_TRIANGLES, 3),
	QUAD(GL11.GL_QUADS, 4);

	final int glId;
	final int vertexCount;

	Primitive(int id, int count) {
		this.glId = id;
		this.vertexCount = count;
	}
}
