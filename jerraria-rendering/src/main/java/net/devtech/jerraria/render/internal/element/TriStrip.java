package net.devtech.jerraria.render.internal.element;

import static java.lang.Math.min;
import static java.lang.Math.pow;
import static net.devtech.jerraria.util.math.JMath.ifloor;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.buffers.ElementBufferBuilder;

public class TriStrip extends ShapeStrat {
	public static final TriStrip BYTE_ = new TriStrip( ShapeStrat.BYTE, 1,  GL_UNSIGNED_BYTE);
	public static final TriStrip SHORT_ = new TriStrip(ShapeStrat.SHORT, 2,  GL_UNSIGNED_SHORT);
	public static final TriStrip INT_ = new TriStrip(ShapeStrat.INT, 4, GL_UNSIGNED_INT);

	protected TriStrip(BufferInserter inserter, int unitLen, int type) {
		super(new ElementBufferBuilder(unitLen, ifloor(min(pow(256, unitLen), 16384)) * unitLen), inserter, type);
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
	void ensureCapacity0(int vertices) {
		int primitives = this.builder.getVertexCount() / 3;
		for(int i = primitives; i < vertices - 2; i++) {
			ByteBuffer buffer = this.builder.getBuffer();
			this.inserter.insert(buffer, i);
			this.builder.next();
			this.inserter.insert(buffer, i + 1);
			this.builder.next();
			this.inserter.insert(buffer, i + 2);
			this.builder.next();
		}
	}
}
