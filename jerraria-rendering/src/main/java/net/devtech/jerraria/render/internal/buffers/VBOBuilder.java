package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;

import java.nio.ByteBuffer;
import java.util.Objects;

public final class VBOBuilder extends AbstractBOBuilder {
	int size = 0;

	public VBOBuilder(VBOBuilder builder) {
		super(builder);
		this.size = builder.size;
	}

	public VBOBuilder(AbstractBOBuilder buffer, int copyCount) {
		super(buffer, copyCount);
		this.size = copyCount;
	}

	public VBOBuilder(int[] offsets, int vertexLen) {
		super(vertexLen, vertexLen, offsets, 0, 8192);
	}

	public boolean bind() {
		int orig = this.glId;
		if(!this.flush()) {
			this.bindBuffer(this.getGlId());
		}
		return orig != this.glId;
	}

	public int getVertexCount() {
		return this.size;
	}

	public ByteBuffer offset(int index) {
		this.variable(index);
		return this.getBuffer();
	}

	public void vert() {
		this.struct(this.size++);
	}

	public void assertElementRange(int from, int to) {
		Objects.checkFromToIndex(from, to, this.size);
	}

	public void reset() {
		this.size = 0;
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
