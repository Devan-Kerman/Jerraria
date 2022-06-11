package net.devtech.jerraria.render.internal.element;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glGenBuffers;

import java.nio.ByteBuffer;

import net.devtech.jerraria.render.api.DrawMethod;
import net.devtech.jerraria.render.internal.buffers.EBOBuilder;

public abstract class ShapeStrat {
	public static final BufferInserter BYTE = (b, i) -> b.put((byte) i);
	public static final BufferInserter SHORT = (b, i) -> b.putShort((short) i);
	public static final BufferInserter INT = ByteBuffer::putInt;
	public final EBOBuilder builder;
	final BufferInserter inserter;
	final int type;

	protected ShapeStrat(EBOBuilder builder, BufferInserter inserter, int type) {
		this.builder = builder;
		this.inserter = inserter;
		this.type = type;
	}

	public void ensureCapacity(int elements) {
		this.ensureCapacity0(elements);
	}

	public abstract int elementsForVertexData(int count);

	public abstract int vertexCount(DrawMethod method);

	public abstract int minumumVertices(DrawMethod method);

	abstract void ensureCapacity0(int elements);

	public void bind() {
		this.builder.bind();
	}

	public int getType() {
		return this.type;
	}

	public int maxSize() {
		return maxSize(this.type);
	}

	public static int maxSize(int type) {
		return switch(type) {
			case GL_UNSIGNED_BYTE -> 256;
			case GL_UNSIGNED_SHORT -> 65536;
			case GL_UNSIGNED_INT -> Integer.MAX_VALUE;
			default -> 0;
		};
	}

	public interface BufferInserter {
		void insert(ByteBuffer buffer, int value);
	}
}
