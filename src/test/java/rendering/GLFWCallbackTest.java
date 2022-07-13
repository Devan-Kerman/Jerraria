package rendering;

import org.lwjgl.glfw.GLFW;

public class GLFWCallbackTest {
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			long window = JerrariaClient.MAIN_WINDOW_GL_ID;
			StringBuilder builder = new StringBuilder();
			GLFW.glfwSetCharCallback(window, (window1, codepoint) -> {
				builder.appendCodePoint(codepoint);
				System.out.println(builder);
			});
			GLFW.glfwSetKeyCallback(window, (window1, key, scancode, action, mods) -> {
				System.out.println("AA " + GLFW.glfwGetKeyName(key, scancode) + " " + action);
			});
			return null;
		});
	}
}
