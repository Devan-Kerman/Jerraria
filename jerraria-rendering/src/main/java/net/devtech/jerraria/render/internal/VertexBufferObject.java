package net.devtech.jerraria.render.internal;

import java.util.List;

import it.unimi.dsi.fastutil.ints.IntList;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.internal.buffers.VBOBuilder;

class VertexBufferObject {
	final List<GlData.Element> elements;
	final String name;
	final VBOBuilder buffer;
	final int byteLength;

	public VertexBufferObject(String name, IntList offsets, int vertexLen, List<GlData.Element> elements) {
		this.name = name;
		this.buffer = new VBOBuilder(offsets.toIntArray(), vertexLen);
		this.elements = elements;
		this.byteLength = vertexLen;
	}

	public VertexBufferObject(VertexBufferObject group, boolean copyContents) {
		this.name = group.name;
		this.elements = group.elements;
		this.byteLength = group.byteLength;
		if(copyContents) {
			this.buffer = new VBOBuilder(group.buffer);
		} else {
			this.buffer = new VBOBuilder(group.buffer, 0);
		}
	}

	public VBOBuilder getBuilder() {
		return this.buffer;
	}

	public boolean bind() {
		return this.buffer.bind();
	}

	public void close() {
		this.buffer.close();
	}

	public int getOffset(int index) {
		return this.buffer.structIntervals[index];
	}
}
