package rendering;

import static org.lwjgl.opengl.GL11.GL_ALWAYS;
import static org.lwjgl.opengl.GL11.GL_GEQUAL;
import static org.lwjgl.opengl.GL11.GL_GREATER;
import static org.lwjgl.opengl.GL11.GL_LEQUAL;
import static org.lwjgl.opengl.GL11.GL_LESS;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.translucency.TranslucentShader;
import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.util.func.TRunnable;
import net.devtech.jerraria.util.math.Matrix3f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.opengl.GL46;
import org.lwjgl.opengl.GLUtil;

public class TranslucencyAPI {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.RESIZE.andThen(TestTranslucentShader.HANDLER::frameSize);
			RenderThread.addRenderStage(TRunnable.of(() -> {
				TestTranslucentShader.HANDLER.renderStart();
				int[] colors = {
					0x80FF0000,
					0x80FFFF00,
					0x8000FF00,
					0x8000FFFF,
					0x800000FF,
					0x80FF00FF,
					0x80FFFFFF,
					0x80AAAAAA
				};

				Matrix3f idt = new Matrix3f();

				SolidColorShader background = SolidColorShader.INSTANCE;
				background.drawRect(idt, -1, -1, 2, 2, 0xFFFFFFFF);
				background.draw();

				var rendering = TestTranslucentShader.INSTANCE;
				for(float i = 0; i < 1; i += .125) {
					rendering.square(idt, i / 2f, i / 2f, .5f, .5f, i, colors[(int) (i * 8)]);
				}

				rendering.draw();
				TestTranslucentShader.HANDLER.renderResolve();
			}), 10);

			return null;
		});
	}

}
