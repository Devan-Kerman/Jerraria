package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

import java.nio.ByteBuffer;

public final class VBOBuilder extends AbstractBOBuilder {
	int element = 0;

	public VBOBuilder(VBOBuilder builder) {
		super(builder);
		this.element = builder.element;
	}

	public VBOBuilder(AbstractBOBuilder buffer, int copyCount) {
		super(buffer, copyCount);
		this.element = copyCount;
	}

	public VBOBuilder(int[] offsets, int vertexLen) {
		super(vertexLen, vertexLen, offsets, 0);
	}

	public boolean bind() {
		int orig = this.glId;
		if(!this.flush()) {
			this.bindBuffer(this.glId);
		}
		return orig != this.glId;
	}

	public int getVertexCount() {
		return this.element;
	}

	public ByteBuffer offset(int index) {
		this.variable(index);
		return this.getBuffer();
	}

	public void vert() {
		this.struct(this.element++);
	}

	public void reset() {
		this.element = 0;
	}

	@Override
	protected int bindTarget() {
		return GL_ARRAY_BUFFER;
	}

	@Override
	protected int padding() {
		return 1;
	}
}
