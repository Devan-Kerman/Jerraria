package rendering.bloom;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.shaders.ColoredTextureShader;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.util.math.Matrix3f;

public class BlurRenderering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}
	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.addRenderStage(() -> {
				BloomRenderer.renderBloom(() -> {
					SolidColorShader shader = SolidColorShader.INSTANCE;
					shader.rect(new Matrix3f(), 0, 0, 1, 1, 0xFFFFFFFF);
					shader.draw();
				});
				BloomRenderer.drawBloomToMain();
			}, 10);

			return null;
		});
	}
}
