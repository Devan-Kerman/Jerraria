package net.devtech.jerraria.render.api;

import static org.lwjgl.opengl.GL46.*;

public class OpenGLSupport {
	public static final int MAJOR_V = glGetInteger(GL_MAJOR_VERSION);
	public static final int MINOR_V = glGetInteger(GL_MINOR_VERSION);
	public static final boolean ATOMIC_COUNTERS = greaterThan(4, 2);
	public static final boolean ATOMIC_COUNTERS_PLUS = greaterThan(4, 6);
	public static final boolean IMAGE_LOAD_STORE = greaterThan(4, 2);
	public static final boolean IMAGE_LOAD_SIZE = greaterThan(4, 3);
	public static final boolean BLEND_FUNC_I = greaterThan(4, 0);

	public static boolean greaterThan(int major, int minor) {
		return MAJOR_V > major || (MAJOR_V == major && MINOR_V >= minor);
	}
}
