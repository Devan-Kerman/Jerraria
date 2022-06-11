package net.devtech.jerraria.render.internal.element;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.buffers.EBOBuilder;

public class LineStrip extends ShapeStrat {
	public static final LineStrip BYTE_ = new LineStrip(ShapeStrat.BYTE, 1,  GL_UNSIGNED_BYTE);
	public static final LineStrip SHORT_ = new LineStrip(ShapeStrat.SHORT, 2,  GL_UNSIGNED_SHORT);
	public static final LineStrip INT_ = new LineStrip(ShapeStrat.INT, 4, GL_UNSIGNED_INT);

	protected LineStrip(BufferInserter inserter, int unitLen, int type) {
		super(new EBOBuilder(unitLen), inserter, type);
	}

	@Override
	public int elementsForVertexData(int count) {
		return (count-1)*2;
	}

	@Override
	public int vertexCount(DrawMethod method) {
		return 1;
	}

	@Override
	public int minumumVertices(DrawMethod method) {
		return 2;
	}

	@Override
	void ensureCapacity0(int elements) {
		int primitives = this.builder.getElementCount() / 2;
		for(int i = primitives; i < elements - 1; i++) {
			ByteBuffer buffer = this.builder.getBuffer();
			this.inserter.insert(this.builder.vert(), i);
			this.inserter.insert(this.builder.vert(), i + 1);
		}
	}
}

