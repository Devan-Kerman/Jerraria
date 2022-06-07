package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.util.func.TRunnable;
import net.devtech.jerraria.util.math.Matrix3f;

public class TranslucencyAPI {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.addRenderStage(TRunnable.of(() -> {
				TestTranslucentShader.HANDLER.renderStart();
				int[] colors = {0x80FF0000, 0x80FFFF00, 0x8000FF00, 0x8000FFFF,
				                0x800000FF, 0x80FF00FF, 0x80FFFFFF, 0x80AAAAAA};

				TestTranslucentShader rendering = TestTranslucentShader.INSTANCE;
				Matrix3f idt = new Matrix3f();
				for(float i = 0; i < 1; i+=.125) {
					rendering.square(idt, i/2f, i/2f, .5f, .5f, i, colors[(int) (i * 8)]);
				}
				rendering.draw();

				SolidColorShader background = SolidColorShader.INSTANCE;
				background.drawRect(idt, -1, -1, 2, 2, 0xFFFFFFFF);
				background.draw();

				TestTranslucentShader.HANDLER.renderResolve();
				rendering.flushFrameBuffer();
			}), 10);

			return null;
		});
	}

}
