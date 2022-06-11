package net.devtech.jerraria.render.internal.element;

import net.devtech.jerraria.render.api.DrawMethod;
import org.lwjgl.opengl.GL30;

public class DataUpload extends ShapeStrat {
	public static final DataUpload INSTANCE = new DataUpload();
	protected DataUpload() {
		super(null, (buffer, value) -> {}, GL30.GL_UNSIGNED_BYTE);
	}

	@Override
	public int elementsForVertexData(int count) {
		return 0; // upload only
	}

	@Override
	public int vertexCount(DrawMethod method) {
		return 1; // upload only
	}

	@Override
	public int minumumVertices(DrawMethod method) {
		return 0; // upload only
	}

	@Override
	void ensureCapacity0(int elements) {
	}

	@Override
	public void bind() {
		throw new UnsupportedOperationException();
	}

	@Override
	public int getType() {
		return super.getType();
	}

	@Override
	public int maxSize() {
		return super.maxSize();
	}
}
