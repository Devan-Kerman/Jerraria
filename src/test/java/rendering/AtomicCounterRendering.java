package rendering;


import static org.lwjgl.opengl.GL46.*;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.ClientMain;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.util.math.Mat;
import net.devtech.jerraria.util.math.Mat2x3f;
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
			Mat mat = ClientMain.cartesianToAWTIndexGrid(1);
			shader.drawRect(mat, 0, 0, 1, 1, 0xFFFFFFFF);
			RenderThread.addRenderStage(shader::drawKeep, 10);
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
