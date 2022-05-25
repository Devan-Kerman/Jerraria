package net.devtech.jerraria.render.internal;

import java.lang.ref.Cleaner;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.lwjgl.opengl.GL15;

public class BufferReclamation {
	private static final IntList RECLAIMED_BUFFERS = new IntArrayList();
	public static Cleaner.Cleanable manageBuffer(Object object, int glId) {
		return BareShader.GL_CLEANUP.register(object, () -> {
			synchronized(RECLAIMED_BUFFERS) {
				RECLAIMED_BUFFERS.add(glId);
			}
		});
	}

	public static void reclaimBuffers() {
		synchronized(RECLAIMED_BUFFERS) {
			if(!RECLAIMED_BUFFERS.isEmpty()) {
				GL15.glDeleteBuffers(RECLAIMED_BUFFERS.toIntArray());
				RECLAIMED_BUFFERS.clear();
			}
		}
	}
}
