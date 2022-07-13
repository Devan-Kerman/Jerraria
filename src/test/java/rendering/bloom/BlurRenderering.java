package rendering.bloom;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;
import net.devtech.jerraria.util.math.Mat2x3f;

public class BlurRenderering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.addRenderStage(() -> {
				BloomRenderer.renderBloom(() -> {
					SolidColorShader shader = SolidColorShader.INSTANCE;
					shader.rect(new Mat2x3f(), 0, 0, 1, 1, 0xFFFFFFFF);
					shader.draw();
				});
				BloomRenderer.drawBloomToMain();
			}, 10);

			return null;
		});
	}
}
