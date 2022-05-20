package rendering;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.ClientMain;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.shaders.SolidColorShader;
import net.devtech.jerraria.util.math.Matrix3f;

public class BasicRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			SolidColorShader shader = SolidColorShader.INSTANCE;
			Matrix3f mat = ClientMain.cartesianToAWTIndexGrid(1);
			shader.drawRect(mat, 0, 0, 1, 1, 0xFFFFFFFF);
			RenderThread.addRenderStage(shader::render, 10);
			return null;
		});
	}
}
