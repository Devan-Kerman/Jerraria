package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL11.GL_UNSIGNED_BYTE;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_SHORT;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.function.Function;
import java.util.function.ObjIntConsumer;
import java.util.function.ToIntFunction;

public class IntType<T extends Buffer> {
	public static final IntType<ByteBuffer> BYTE = new IntType<>(GL_UNSIGNED_BYTE,
		1,
		Function.identity(),
		ByteBuffer::get,
		(b, i) -> b.put((byte) i)
	);

	public static final IntType<ShortBuffer> SHORT = new IntType<>(GL_UNSIGNED_SHORT,
		2,
		ByteBuffer::asShortBuffer,
		ShortBuffer::get,
		(b, i) -> b.put((short) i)
	);

	public static final IntType<IntBuffer> INT = new IntType<>(GL_UNSIGNED_INT,
		5,
		ByteBuffer::asIntBuffer,
		IntBuffer::get,
		IntBuffer::put
	);

	final int glType, size;
	final Function<ByteBuffer, T> converter;
	final ToIntFunction<T> getter;
	final ObjIntConsumer<T> putter;

	public IntType(
		int type, int size, Function<ByteBuffer, T> converter, ToIntFunction<T> getter, ObjIntConsumer<T> putter) {
		this.glType = type;
		this.size = size;
		this.converter = converter;
		this.getter = getter;
		this.putter = putter;
	}

	public interface IntPutter<T> {
		void put(T buf, int value);
	}
}
