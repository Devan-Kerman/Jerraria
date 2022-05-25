package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.render.api.basic.GlData;

class VertexBufferObject {
	final List<GlData.Element> elements;
	final String name;
	private BufferObjectBuilder buffer;
	int byteLength;

	public VertexBufferObject(String name) {
		this.name = name;
		this.elements = new ArrayList<>();
	}

	public VertexBufferObject(VertexBufferObject group, boolean copyContents) {
		this.name = group.name;
		this.elements = group.elements;
		this.byteLength = group.byteLength;
		if(copyContents) {
			// todo validate check on thread
			this.buffer = new BufferObjectBuilder(group.buffer);
		} else {
			this.buffer = BufferObjectBuilder.vaoBound(GL_ARRAY_BUFFER, group.byteLength);
		}
	}

	public BufferObjectBuilder getBuilder() {
		BufferObjectBuilder buffer = this.buffer;
		if(buffer == null) {
			return this.buffer = BufferObjectBuilder.vaoBound(GL_ARRAY_BUFFER, this.byteLength);
		} else {
			return buffer;
		}
	}

	public boolean bindAndUpload() {
		if(this.buffer == null) {
			return false;
		}
		return this.buffer.upload(true);
	}
}
