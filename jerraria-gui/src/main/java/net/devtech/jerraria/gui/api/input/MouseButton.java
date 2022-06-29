package net.devtech.jerraria.gui.api.input;

import org.jetbrains.annotations.ApiStatus;
import org.lwjgl.glfw.GLFW;

/**
 * Can be compared by reference
 */
public interface MouseButton {
	/**
	 * @see GLFW#GLFW_MOUSE_BUTTON_1
	 */
	@ApiStatus.Internal
	int glfwId();

	/**
	 * All the names after MIDDLE are approximations and may not be the same for all mouses. They are simply
	 * human-recognizable names
	 */
	enum Standard implements MouseButton {
		LEFT(GLFW.GLFW_MOUSE_BUTTON_LEFT),
		RIGHT(GLFW.GLFW_MOUSE_BUTTON_RIGHT),
		MIDDLE(GLFW.GLFW_MOUSE_BUTTON_MIDDLE),
		BACK(GLFW.GLFW_MOUSE_BUTTON_4),
		FORWARD(GLFW.GLFW_MOUSE_BUTTON_5),
		DPI_SWITCH(GLFW.GLFW_MOUSE_BUTTON_6),
		BUTTON_7(GLFW.GLFW_MOUSE_BUTTON_7),
		BUTTON_8(GLFW.GLFW_MOUSE_BUTTON_8),
		;
		final int glfwId;

		Standard(int id) {
			this.glfwId = id;
		}

		Standard(Standard pseudo) {
			this(pseudo.glfwId);
		}

		@Override
		public int glfwId() {
			return this.glfwId;
		}
	}
}
