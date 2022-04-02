package net.devtech.jerraria.client;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;

import net.devtech.jerraria.client.render.textures.Atlas;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class RenderThread {
	private static final List<Runnable> RENDER_QUEUE = new Vector<>();
	private static final Set<RenderStage> STAGES = new ConcurrentSkipListSet<>(Comparator.comparingInt(r -> r.id));

	static void startRender() {
		while(!GLFW.glfwWindowShouldClose(ClientInit.glMainWindow)) {
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GLFW.glfwPollEvents();
			long src = System.currentTimeMillis();
			for(Atlas value : Atlas.getAtlases().values()) {
				value.updateAnimation(src);
			}

			for(RenderStage stage : STAGES) {
				stage.runnable.run();
			}

			for(int i = RENDER_QUEUE.size() - 1; i >= 0; i--) {
				RENDER_QUEUE.remove(i).run();
			}
			GLFW.glfwSwapBuffers(ClientInit.glMainWindow);
		}
	}

	public static void queueRenderTask(Runnable task) {
		RENDER_QUEUE.add(task);
	}

	public static void addRenderStage(Runnable runnable, int priority) {
		STAGES.add(new RenderStage(priority, runnable));
	}

	public static void removeRenderStage(Runnable runnable) {
		STAGES.remove(new RenderStage(0, runnable));
	}

	record RenderStage(int id, Runnable runnable) {
		@Override
		public boolean equals(Object o) {
			return this == o || o instanceof RenderStage render && this.runnable.equals(render.runnable);
		}

		@Override
		public int hashCode() {
			return this.runnable.hashCode();
		}
	}
}
