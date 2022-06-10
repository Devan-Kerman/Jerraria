package net.devtech.jerraria.render.api;

import static org.lwjgl.opengl.GL46.*;

import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLCapabilities;

/**
 * List of supported or unsupported OpenGL features
 */
public class OpenGLSupport { // todo use gl capabilities
	public static final GLCapabilities CAPABILITIES = GL.getCapabilities();
	public static final int MAJOR_V = glGetInteger(GL_MAJOR_VERSION);
	public static final int MINOR_V = glGetInteger(GL_MINOR_VERSION);
	public static final boolean ATOMIC_COUNTERS = CAPABILITIES.GL_ARB_shader_atomic_counters;
	public static final boolean ATOMIC_COUNTERS_PLUS = CAPABILITIES.GL_ARB_shader_atomic_counter_ops;
	public static final boolean IMAGE_LOAD_STORE = CAPABILITIES.GL_ARB_shader_image_load_store;
	public static final boolean IMAGE_LOAD_SIZE = CAPABILITIES.GL_ARB_shader_image_size;
	public static final boolean BLEND_FUNC_I = CAPABILITIES.GL_ARB_blend_func_extended;
	public static final boolean SSBO = CAPABILITIES.GL_ARB_shader_storage_buffer_object;

	public static boolean coreSince(int major, int minor) {

		return MAJOR_V > major || (MAJOR_V == major && MINOR_V >= minor);
	}
}
