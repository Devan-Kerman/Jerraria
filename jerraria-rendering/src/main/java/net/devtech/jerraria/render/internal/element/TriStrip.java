package net.devtech.jerraria.render.internal.element;

import static java.lang.Math.min;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.buffers.EBOBuilder;

public class TriStrip extends ShapeStrat {
	public static final TriStrip BYTE_ = new TriStrip( ShapeStrat.BYTE, 1,  GL_UNSIGNED_BYTE);
	public static final TriStrip SHORT_ = new TriStrip(ShapeStrat.SHORT, 2,  GL_UNSIGNED_SHORT);
	public static final TriStrip INT_ = new TriStrip(ShapeStrat.INT, 4, GL_UNSIGNED_INT);

	protected TriStrip(BufferInserter inserter, int unitLen, int type) {
		super(new EBOBuilder(unitLen), inserter, type);
	}

	@Override
	public int elementsForVertexData(int count) {
		return (count-2)*3;
	}

	@Override
	public int vertexCount(DrawMethod method) {
		return 1;
	}

	@Override
	public int minumumVertices(DrawMethod method) {
		return 3;
	}

	@Override
	void ensureCapacity0(int elements) {
		int primitives = this.builder.getElementCount() / 3;
		for(int i = primitives; i < elements - 2; i++) {
			this.inserter.insert(this.builder.vert(), i);
			this.inserter.insert(this.builder.vert(), i + 1);
			this.inserter.insert(this.builder.vert(), i + 2);
		}
	}
}
