package net.devtech.jerraria.render.internal.element;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.buffers.ElementBufferBuilder;
import net.devtech.jerraria.util.math.JMath;

public final class Quad extends ShapeStrat {
	public static final Quad BYTE_ = new Quad(1,  256, GL_UNSIGNED_BYTE, ShapeStrat.BYTE);
	public static final Quad SHORT_ = new Quad(2,  65536, GL_UNSIGNED_SHORT, ShapeStrat.SHORT);
	public static final Quad INT_ = new Quad(4, 536870911, GL_UNSIGNED_INT, ShapeStrat.INT);

	final long maxSize;

	Quad(int unitLen, int maxSize, int glType, BufferInserter inserter) {
		super(new ElementBufferBuilder(unitLen, Math.min(maxSize, 16384) * unitLen * 6), inserter, glType);
		this.maxSize = maxSize;
		this.ensureCapacity(Math.min(maxSize, 16384));
	}

	@Override
	public int elementsForVertexData(int vertexDataCount) {
		return (vertexDataCount*6)/4;
	}

	@Override
	public int vertexCount(DrawMethod method) {
		return 4;
	}

	@Override
	public int minumumVertices(DrawMethod method) {
		return 4;
	}

	@Override
	public void ensureCapacity0(int vertices) {
		if(vertices > this.maxSize) {
			throw new IllegalStateException("size is too big for quad type!");
		}

		int primitives = this.builder.getVertexCount() / 6;
		for(int i = primitives; i < JMath.ceilDiv(vertices, 6); i++) {
			ByteBuffer buffer = this.builder.getBuffer();
			this.inserter.insert(buffer, i * 4);
			this.builder.next();
			this.inserter.insert(buffer, i * 4 + 1);
			this.builder.next();
			this.inserter.insert(buffer, i * 4 + 2);
			this.builder.next();
			this.inserter.insert(buffer, i * 4 + 2);
			this.builder.next();
			this.inserter.insert(buffer, i * 4 + 3);
			this.builder.next();
			this.inserter.insert(buffer, i * 4);
			this.builder.next();
		}
	}
}
