package net.devtech.jerraria.client;

import java.util.List;
import java.util.Vector;

import net.devtech.jerraria.client.render.api.Primitive;
import net.devtech.jerraria.client.render.textures.Atlas;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class RenderThread {
	private static final List<Runnable> RENDER_QUEUE = new Vector<>();

	static void startRender() {
		while(!GLFW.glfwWindowShouldClose(ClientInit.glMainWindow)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GLFW.glfwPollEvents();
			long src = System.currentTimeMillis();
			for(Atlas value : Atlas.getAtlases().values()) {
				value.updateAnimation(src);
			}


			GLFW.glfwSwapBuffers(ClientInit.glMainWindow);
		}
	}
}
