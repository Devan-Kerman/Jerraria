package net.devtech.jerraria.render.internal.buffers;

import net.devtech.jerraria.util.math.JMath;
import org.lwjgl.opengl.GL33;

public class StaticBuffers {
	private static final int EMPTY_BUFFER_ID = GL33.glGenBuffers();
	private static int emptyBufferCapacity;

	public static int emptyBuffer(int capacity) {
		if(emptyBufferCapacity < capacity) {
			GL33.glBindBuffer(GL33.GL_COPY_WRITE_BUFFER, EMPTY_BUFFER_ID);
			GL33.glBufferData(GL33.GL_COPY_WRITE_BUFFER, JMath.nearestPowerOf2(capacity), GL33.GL_STATIC_DRAW);
		}
		return EMPTY_BUFFER_ID;
	}
}
