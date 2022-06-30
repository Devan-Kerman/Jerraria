package net.devtech.jerraria.client;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.Executor;

import net.devtech.jerraria.access.Access;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.client.render.textures.Atlas;
import net.devtech.jerraria.util.Validate;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL11;

public class RenderThread {
	private static final List<Runnable> RENDER_QUEUE = new Vector<>();
	private static final Set<RenderStage> STAGES = new ConcurrentSkipListSet<>(Comparator.comparingInt(r -> r.id));
	public static final Executor EXECUTOR = RenderThread::queueRenderTask;
	public static final Access<ResizeListener> RESIZE = Access.create(array -> (width, height) -> {
		for(ResizeListener listener : array) {
			listener.onResize(width, height);
		}
	});

	public interface ResizeListener {
		void onResize(int width, int height);
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

	static void startRender() {
		while(!GLFW.glfwWindowShouldClose(ClientInit.glMainWindow)) {
			GLContextState.bindDefaultFrameBuffer();
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT | GL11.GL_STENCIL_BUFFER_BIT);

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

			GLContextState.DEPTH_MASK.set(true); // idk why I need this but I do so here it is
			GLFW.glfwSwapBuffers(ClientInit.glMainWindow);
			GLFW.glfwPollEvents();

			if(Validate.IN_DEV) {
				System.gc();
			}
		}
	}

	record RenderStage(int id, Runnable runnable) {
		@Override
		public boolean equals(Object o) {
			if(this == o) {
				return true;
			}
			return o instanceof RenderStage render && this.runnable.equals(render.runnable);
		}

		@Override
		public int hashCode() {
			return this.runnable.hashCode();
		}
	}
}
