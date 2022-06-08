package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.internal.buffers.EBOBuilder;
import net.devtech.jerraria.render.internal.element.ShapeStrat;

// todo move EBO into VAO class?
public class ElementData {
	int currentType;
	EBOBuilder builder;

	public ElementData() {
		this.builder = new EBOBuilder(1);
		this.currentType = GL_UNSIGNED_BYTE;
	}

	public ElementData(ElementData ebo) {
		this.builder = new EBOBuilder(ebo.builder);
		this.currentType = ebo.currentType;
	}

	public ElementData(ShapeStrat strat, int elements) {
		this.builder = new EBOBuilder(strat.builder, elements);
		this.currentType = strat.getType();
	}

	public void bind() {
		this.builder.bind();
	}

	public void append(ShapeStrat strat, int from, int len) {
		this.ensureCanIndex(Math.max(strat.maxSize(), ShapeStrat.maxSize(this.currentType))-1);
		this.builder.copyVertexes(strat.builder, from, len);
	}

	private void ensureCanIndex(int index) {
		if(index >= 65536 && this.currentType < GL_UNSIGNED_INT) {
			// resize to int
			EBOBuilder current = this.builder;
			ByteBuffer buffer = current.getBuffer();
			EBOBuilder new_ = new EBOBuilder(4);
			buffer.flip();
			if(this.currentType == GL_UNSIGNED_SHORT) { // short -> int
				for(int i = 0; i < current.getElementCount(); i++) {
					new_.vert().putInt(buffer.getShort());
				}
			} else { // byte -> int
				for(int i = 0; i < current.getElementCount(); i++) {
					new_.vert().putInt(buffer.get());
				}
			}
			this.builder = new_;
			this.currentType = GL_UNSIGNED_INT;
		} else if(index >= 256 && this.currentType < GL_UNSIGNED_SHORT) {
			// resize to short
			EBOBuilder current = this.builder;
			ByteBuffer buffer = current.getBuffer();
			EBOBuilder new_ = new EBOBuilder(2);
			buffer.flip();
			for(int i = 0; i < current.getElementCount(); i++) { // byte -> short
				new_.vert().putShort(buffer.get());
			}
			this.builder = new_;
			this.currentType = GL_UNSIGNED_SHORT;
		}
	}

	public void close() {
		this.builder.flush();
	}
}
