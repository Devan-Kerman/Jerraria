package rendering.swing;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import javax.swing.JButton;
import javax.swing.JFrame;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.Validate;
import org.lwjgl.glfw.GLFW;

public class SwingTesting {
	static {
		//System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}


	public static void main(String[] args) {
		System.setProperty("sun.java2d.opengl", "true");
		//JFrame frame2 = new JFrame();
		//frame2.setSize(100, 100);
		//frame2.setVisible(true);
		//frame2.add(new JButton());
		//frame2.dispose();
		Bootstrap.startClient(args, () -> {

			//JOptionPane.showConfirmDialog(null, "bruh");
			for(Thread thread : Thread.getAllStackTraces().keySet()) {
				String name = thread.getName();
				if(name.equals("Java2D Queue Flusher")) {
					Module module = SwingTesting.class.getModule();
					UnsafeReflection.startUnsafe(SwingTesting.class, Thread.class);
					MethodHandles.Lookup lookup = MethodHandles.lookup();
					MethodHandles.Lookup privateLookup = MethodHandles.privateLookupIn(Thread.class, lookup);
					VarHandle target = privateLookup.findVarHandle(Thread.class, "target", Runnable.class);
					Runnable sync = (Runnable) target.getAndSet(thread, null);
					System.out.println(target.get(thread));
					UnsafeReflection.endUnsafe(SwingTesting.class, module);
					thread.stop();
					Class<? extends Runnable> aClass = sync.getClass();
					UnsafeReflection.startUnsafe(SwingTesting.class, aClass);
					privateLookup = MethodHandles.privateLookupIn(aClass, lookup);
					target = privateLookup.findVarHandle(aClass, "task", Runnable.class);
					VarHandle needsFlush = privateLookup.findVarHandle(aClass, "needsFlush", boolean.class);
					UnsafeReflection.endUnsafe(SwingTesting.class, module);
					VarHandle finalTarget = target;
					new Thread(() -> {
						while(true) {
							finalTarget.compareAndSet(sync, null, (Runnable) () -> {
								Validate.rethrow(new Throwable());
							});
							needsFlush.compareAndSet(sync, false, true);
							try {
								Thread.sleep(100);
							} catch(InterruptedException e) {
								throw new RuntimeException(e);
							}
						}
					}).start();

					RenderThread.queueRenderTask(() -> {
						new Thread(() -> {
							JFrame frame = new JFrame();
							frame.setSize(100, 100);
							frame.setVisible(true);
							frame.add(new JButton());
							try {
								Thread.sleep(1000000);
							} catch(InterruptedException e) {
								throw new RuntimeException(e);
							}
						}).start();
						//JOptionPane.showConfirmDialog(null, "bruh");
					});

					RenderThread.addRenderStage(() -> {
						GLFW.glfwMakeContextCurrent(JerrariaClient.MAIN_WINDOW_GL_ID);
						GLContextState.bindDefaultFrameBuffer();
						try {
							sync.run();
						} catch(Throwable t) {
						}
					}, 10);


				}
			}
			//((WGLSurfaceData.WGLWindowSurfaceData) ((SunGraphics2D) g).surfaceData).getContext().rq.flushAndInvokeNow(() -> {System.out.println(GLFW.glfwGetCurrentContext());});
			return null;
		});
	}

	public interface TRunnable extends Runnable {
		@Override
		void run();
	}
}
