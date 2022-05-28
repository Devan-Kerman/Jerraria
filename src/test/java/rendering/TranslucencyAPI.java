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
				int[] colors = {0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
				                0x0000FF, 0xFF00FF, 0xFFFFFF, 0xAAAAAA};

				TestTranslucentShader rendering = TestTranslucentShader.INSTANCE;
				rendering.color.vec4f(.2f, .8f, .5f, .5f);
				Matrix3f identity = new Matrix3f();
				for(float i = 0; i < 1; i+=.125) {
					rendering.square(identity, i/2f, i/2f, .5f, .5f, -i);
				}
				rendering.draw();

				SolidColorShader background = SolidColorShader.INSTANCE;
				background.drawRect(identity, -1, -1, 2, 2, 0xFFFFFFFF);
				background.draw();

				TestTranslucentShader.HANDLER.renderResolve();
				rendering.flushFrameBuffer();
			}), 10);

			return null;
		});
	}

}
