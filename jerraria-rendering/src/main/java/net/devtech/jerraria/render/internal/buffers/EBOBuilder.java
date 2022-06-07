package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;

import java.nio.ByteBuffer;

public final class EBOBuilder extends AbstractBOBuilder {
	private static final int[] OFFSET = {0};
	int element;

	public EBOBuilder(EBOBuilder builder) {
		this(builder, builder.getElementCount());
	}

	public EBOBuilder(EBOBuilder builder, int vertices) {
		super(builder, vertices);
		this.element = builder.element;
	}

	public EBOBuilder(int vertexLength) {
		super(vertexLength, vertexLength, OFFSET, 0, 256);
	}

	public void copyVertexes(EBOBuilder src, int from, int len) {
		this.copyFrom(src, from, this.getElementCount(), 0, 0, len * this.structLen);
		this.mut(from + len);
	}

	public ByteBuffer vert() {
		this.structVariable(this.element++, 0);
		return this.getBuffer();
	}

	public int getElementCount() {
		return this.element;
	}

	public void bind() {
		if(!this.flush()) {
			this.bindBuffer(this.getGlId());
		}
	}

	@Override
	protected int bindTarget() {
		return GL_ELEMENT_ARRAY_BUFFER;
	}

	@Override
	protected int padding() {
		return 1;
	}

	@Override
	public ByteBuffer getBuffer() {
		return this.buffer;
	}
}
