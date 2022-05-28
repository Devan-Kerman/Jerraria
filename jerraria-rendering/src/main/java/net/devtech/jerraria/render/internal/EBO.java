package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL15.*;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.internal.element.ShapeStrat;

// todo move EBO into VAO class?
public class EBO {
	final int glId;
	int currentType;
	BufferBuilder builder;
	boolean isDirty;

	public EBO() {
		this.builder = new BufferBuilder(1, 256);
		this.glId = glGenBuffers();
		this.currentType = GL_UNSIGNED_BYTE;
	}

	public EBO(EBO ebo) {
		this.glId = glGenBuffers();
		this.builder = new BufferBuilder(ebo.builder);
		this.currentType = ebo.currentType;
		this.isDirty = true;
	}

	public EBO(ShapeStrat strat, int elements) {
		this.glId = glGenBuffers();
		this.builder = new BufferBuilder(strat.builder, elements);
		this.currentType = strat.getType();
		this.isDirty = true;
	}

	public void bind() {
		glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, this.glId);
		if(this.isDirty) {
			this.builder.upload(GL_ELEMENT_ARRAY_BUFFER);
		}
	}

	public void append(ShapeStrat strat, int from, int len) {
		this.ensureCanIndex(Math.max(strat.maxSize(), ShapeStrat.maxSize(this.currentType))-1);
		this.builder.copyVertexes(strat.builder, from, len);
	}

	public void append(int index) {
		this.ensureCanIndex(index);
		switch(this.currentType) {
			case GL_UNSIGNED_INT -> this.builder.i(index);
			case GL_UNSIGNED_SHORT -> this.builder.s((short) index);
			case GL_UNSIGNED_BYTE -> this.builder.b((byte) index);
		}
		this.builder.next();
		this.isDirty = true;
	}

	private void ensureCanIndex(int index) {
		if(index >= 65536 && this.currentType < GL_UNSIGNED_INT) {
			// resize to int
			BufferBuilder current = this.builder;
			ByteBuffer buffer = current.buffer;
			BufferBuilder new_ = new BufferBuilder(4, current.vertexCount*4+1024);
			ByteBuffer newBuf = new_.getBuffer();
			buffer.flip();
			if(this.currentType == GL_UNSIGNED_SHORT) { // short -> int
				for(int i = 0; i < current.vertexCount; i++) {
					newBuf.putInt(buffer.getShort());
					new_.next();
				}
			} else { // byte -> int
				for(int i = 0; i < current.vertexCount; i++) {
					newBuf.putInt(buffer.get());
					new_.next();
				}
			}
			this.builder = new_;
			this.currentType = GL_UNSIGNED_INT;
		} else if(index >= 256 && this.currentType < GL_UNSIGNED_SHORT) {
			// resize to short
			BufferBuilder current = this.builder;
			ByteBuffer buffer = current.buffer;
			BufferBuilder new_ = new BufferBuilder(2, current.vertexCount*2+256);
			ByteBuffer newBuf = new_.getBuffer();
			buffer.flip();
			for(int i = 0; i < current.vertexCount; i++) {
				newBuf.putShort(buffer.get());
				new_.next();
			}
			this.builder = new_;
			this.currentType = GL_UNSIGNED_SHORT;
		}
	}

	public void close() {
		glDeleteBuffers(this.glId);
	}
}
