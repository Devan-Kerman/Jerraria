package net.devtech.jerraria.render.internal;

import java.lang.ref.Cleaner;
import java.util.function.Consumer;

import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public class GLReclamation {
	private static final IntList RECLAIMED_BUFFERS = new IntArrayList();
	private static final IntList RECLAIMED_FRAMEBUFFERS = new IntArrayList();

	public static int genBuffer(Object toManage) {
		reclaimBuffers();
		int gen = GL33.glGenBuffers();
		manageBuffer(toManage, gen);
		return gen;
	}

	public static Cleaner.Cleanable manageBuffer(Object object, int glId) {
		return manageId(object, glId, RECLAIMED_BUFFERS);
	}

	public static Cleaner.Cleanable manageFrameBuffer(Object object, int glId) {
		return manageId(object, glId, RECLAIMED_FRAMEBUFFERS);
	}

	private static Cleaner.Cleanable manageId(Object object, int glId, IntList buffer) {
		return BareShader.GL_CLEANUP.register(object, () -> {
			synchronized(buffer) {
				buffer.add(glId);
			}
		});
	}

	public static void reclaimBuffers() {
		reclaimBuffers(RECLAIMED_BUFFERS, GL15::glDeleteBuffers);
	}

	public static void reclaimFrameBuffers() {
		reclaimBuffers(RECLAIMED_FRAMEBUFFERS, GL33::glDeleteFramebuffers);
	}

	private static void reclaimBuffers(final IntList list, Consumer<int[]> ids) {
		synchronized(list) {
			if(!list.isEmpty()) {
				ids.accept(list.toIntArray());
				GL15.glDeleteBuffers(list.toIntArray());
				list.clear();
			}
		}
	}
}
