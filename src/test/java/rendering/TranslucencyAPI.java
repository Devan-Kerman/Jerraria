package rendering;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.util.func.TRunnable;
import net.devtech.jerraria.util.math.Mat2x3f;

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

				Mat2x3f idt = new Mat2x3f();

				SolidColorShader background = SolidColorShader.INSTANCE;
				background.rect(idt, -1, -1, 2, 2, 0xFFFFFFFF);
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
