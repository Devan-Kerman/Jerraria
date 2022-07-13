package net.devtech.jerraria.gui.impl;

import static org.lwjgl.opengl.GL11.GL_VIEWPORT;
import static org.lwjgl.opengl.GL11.glGetIntegerv;

import java.util.Set;

import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.gui.api.WidgetRenderer;
import net.devtech.jerraria.gui.api.input.Modifier;
import net.devtech.jerraria.gui.api.input.MouseButton;
import net.devtech.jerraria.gui.api.input.InputState;
import net.devtech.jerraria.gui.impl.flags.GuiInternal;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.MatView;
import net.devtech.jerraria.util.math.MatrixPool;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWCursorPosCallback;
import org.lwjgl.glfw.GLFWMouseButtonCallback;
import org.lwjgl.glfw.GLFWWindowSizeCallback;

public class ImGuiController {
	public static final ImGuiController CONTROLLER = new ImGuiController();

	static {
		long window = GLFW.glfwGetCurrentContext();
		MouseButtonCallback buttonCallback = CONTROLLER.new MouseButtonCallback();
		buttonCallback.old = GLFW.glfwSetMouseButtonCallback(window, buttonCallback);

		MousePositionCallback positionCallback = CONTROLLER.new MousePositionCallback();
		positionCallback.old = GLFW.glfwSetCursorPosCallback(window, positionCallback);

		ScreenSizeCallback callback = CONTROLLER.new ScreenSizeCallback();
		callback.old = GLFW.glfwSetWindowSizeCallback(window, callback);

		int[] dims = new int[4];
		glGetIntegerv(GL_VIEWPORT, dims);
		CONTROLLER.screenWidth = dims[2];
		CONTROLLER.screenHeight = dims[3];
	}

	private static final int STANDARD_INPUTS = MouseButton.Standard.values().length;
	float mouseX, mouseY, screenWidth, screenHeight;
	final Int2ObjectMap<Set<Modifier>> pressed = new Int2ObjectOpenHashMap<>(STANDARD_INPUTS);
	Int2ObjectMap<Set<Modifier>> prev = new Int2ObjectOpenHashMap<>();

	final MatrixPool cache = new MatrixPool();

	public InputState createInputState(WidgetRenderer renderer) {
		return new InputStateImpl(renderer);
	}

	public void startFrame() {
		this.prev = new Int2ObjectOpenHashMap<>(this.pressed);
	}

	class InputStateImpl extends InputState {
		final WidgetRenderer renderer;

		InputStateImpl(WidgetRenderer renderer) {this.renderer = renderer;}

		@Override
		protected WidgetRenderer renderer() {
			return this.renderer;
		}

		@Override
		public float mouseX() {
			return this.inverseMatrix().mulX(ImGuiController.this.mouseX, ImGuiController.this.mouseY);
		}

		@Override
		public float mouseY() {
			return this.inverseMatrix().mulY(ImGuiController.this.mouseX, ImGuiController.this.mouseY);
		}

		public MatView inverseMatrix() {
			WidgetRenderer renderer = this.renderer();
			MatView matrix = renderer.mat();
			Mat temp = ImGuiController.this.cache.identity(matrix.getType());
			return matrix.inverse(temp);
		}

		@Override
		public boolean isPressed(MouseButton type) {
			return ImGuiController.this.pressed.containsKey(type.glfwId());
		}

		@Override
		public boolean wasPressed(MouseButton type) {
			return ImGuiController.this.prev.containsKey(type.glfwId());
		}

		@Override
		public Set<Modifier> pressedModifiers(MouseButton type) {
			return ImGuiController.this.pressed.get(type.glfwId());
		}

		@Override
		public Set<Modifier> previousModifiers(MouseButton type) {
			return ImGuiController.this.prev.get(type.glfwId());
		}
	}

	class MouseButtonCallback extends GLFWMouseButtonCallback {
		GLFWMouseButtonCallback old;

		@Override
		public void invoke(long window, int button, int action, int mods) {
			if(action == GLFW.GLFW_PRESS) {
				ImGuiController.this.pressed.put(button, GuiInternal.modifiersByGlfwFlags(mods));
			} else {
				ImGuiController.this.pressed.remove(button);
			}

			if(this.old != null) {
				this.old.invoke(window, button, action, mods);
			}
		}
	}

	class MousePositionCallback extends GLFWCursorPosCallback {
		GLFWCursorPosCallback old;

		@Override
		public void invoke(long window, double xpos, double ypos) {
			ImGuiController.this.mouseX = (float) (xpos / ImGuiController.this.screenWidth) * 2 - 1;
			ImGuiController.this.mouseY = -(float) (ypos / ImGuiController.this.screenHeight) * 2 + 1;
			if(this.old != null) {
				this.old.invoke(window, xpos, ypos);
			}
		}
	}

	class ScreenSizeCallback extends GLFWWindowSizeCallback {
		GLFWWindowSizeCallback old;

		@Override
		public void invoke(long window, int width, int height) {
			ImGuiController.this.screenWidth = width;
			ImGuiController.this.screenHeight = height;
			if(this.old != null) {
				this.old.invoke(window, width, height);
			}
		}
	}
}
