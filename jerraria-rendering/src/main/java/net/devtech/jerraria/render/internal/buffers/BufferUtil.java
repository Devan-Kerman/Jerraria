package net.devtech.jerraria.render.internal.buffers;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL33;

public class BufferUtil {
	private static final int EMPTY_BUFFER_ID = GL33.glGenBuffers();
	private static ByteBuffer emptyBuffer = ByteBuffer.allocate(1024);
	private static int emptyBufferCapacity;

	public static int emptyBufferObject(int capacity) {
		if(emptyBufferCapacity < capacity) {
			GL33.glBindBuffer(GL33.GL_COPY_WRITE_BUFFER, EMPTY_BUFFER_ID);
			GL33.glBufferData(GL33.GL_COPY_WRITE_BUFFER, JMath.nearestPowerOf2(capacity + 64), GL33.GL_STATIC_DRAW);
			emptyBufferCapacity = capacity;
		}
		return EMPTY_BUFFER_ID;
	}

	public static ByteBuffer emptyNioBuffer(int capacity) {
		ByteBuffer emptyBuffer = BufferUtil.emptyBuffer;
		if(emptyBuffer.capacity() < capacity) {
			BufferUtil.emptyBuffer = emptyBuffer = ByteBuffer.allocate(JMath.nearestPowerOf2(capacity + 64));
		}
		return emptyBuffer;
	}

	public static ByteBuffer allocateBuffer(int size) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	public static ByteBuffer reallocateBuffer(ByteBuffer old, int size) {
		ByteBuffer buffer = allocateBuffer(size);
		buffer.put(0, old, 0, Math.min(old.capacity(), size));
		return buffer;
	}
}
