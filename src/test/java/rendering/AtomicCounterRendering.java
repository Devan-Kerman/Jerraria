package rendering;


import static org.lwjgl.opengl.GL46.*;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.ClientMain;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.shaders.TestSolidColorShader;
import net.devtech.jerraria.util.math.Matrix3f;
import org.lwjgl.system.MemoryUtil;

public class AtomicCounterRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			glEnable(GL_DEBUG_OUTPUT);
			glDebugMessageCallback((source, type, id, severity, length, message, userParam) -> {
				System.err.println(message);
				System.err.printf("Error! type:%d, sev:%d, msg:%s%n", type, severity, MemoryUtil.memASCII(message));
			}, 0);
			int alloc = allocateImageListHead(100, 100);
			TestSolidColorShader shader = TestSolidColorShader.INSTANCE;
			shader.imgListHead.tex(alloc);
			Matrix3f mat = ClientMain.cartesianToAWTIndexGrid(1);
			shader.drawRect(mat, 0, 0, 1, 1, 0xFFFFFFFF);
			RenderThread.addRenderStage(() -> {
				shader.drawKeep();
				//glMemoryBarrier(GL_SHADER_IMAGE_ACCESS_BARRIER_BIT);
			}, 10);
			return null;
		});
	}

	public static int allocateImageListHead(int width, int height) {
		int id = glGenTextures();
		int type = DataType.UINT_IMAGE_2D.elementType;
		glBindTexture(type, id);
		glTexStorage2D(type, 1, GL_R32UI, width, height);
		return id;
	}

}
