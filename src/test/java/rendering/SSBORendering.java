package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.util.func.TRunnable;

public class SSBORendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			SSBOShader instance = SSBOShader.INSTANCE;
			instance.color.getAt(0).vec4f(.5f, .5f, .5f, .5f);
			RenderThread.addRenderStage(TRunnable.of(() -> {
				instance.scale.getAt(0).f((float) Math.sin(System.currentTimeMillis() / 100d));
				instance.fade.vec4f(.5f, .5f, .5f, .5f);

				instance.strategy(AutoStrat.QUADS);
				instance.vert().vec3f(0, 0, 0);
				instance.vert().vec3f(0, 1, 0);
				instance.vert().vec3f(1, 1, 0);
				instance.vert().vec3f(1, 0, 0);
				instance.drawInstanced(1);
			}), 10);

			return null;
		});
	}
}
