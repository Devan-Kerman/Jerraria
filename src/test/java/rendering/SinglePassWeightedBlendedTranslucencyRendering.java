package rendering;

import static org.lwjgl.opengl.GL42.*;

import net.devtech.jerraria.client.Bootstrap;
import net.devtech.jerraria.client.RenderThread;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.shaders.TestTranslucentShader;
import net.devtech.jerraria.render.shaders.WBTransRecordShader;
import net.devtech.jerraria.render.shaders.WBTransResolveShader;
import net.devtech.jerraria.util.math.Matrix3f;

public class SinglePassWeightedBlendedTranslucencyRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			int revealage = allocateTexture(800, 600, GL_R32F); // doesn't need to get cleared
			int accum = allocateTexture(800, 600, GL_RGBA32F);
			RenderThread.addRenderStage(() -> {
				TestTranslucentShader rendering = TestTranslucentShader.INSTANCE;
				rendering.singlePassWeighted.accum.tex(accum);
				rendering.singlePassWeighted.reveal.tex(revealage);

				Matrix3f identity = new Matrix3f();

				int[] colors = {0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
				                0x0000FF, 0xFF00FF, 0xFFFFFF, 0xAAAAAA};
				for(float i = 0; i < 1; i+=.125) {
					rendering.square(identity, i/2f, i/2f, .5f, .5f, -i, 0x80000000 | colors[(int) (i * 8)]);
				}

				// render to translucency buffer
				glEnable(GL_DEPTH_TEST);
				glDepthFunc(GL_LESS);
				glDepthMask(false);
				glEnable(GL_BLEND);
				glBlendFunci(0, GL_ONE, GL_ONE); // accumulation blend target
				glBlendFunci(1, GL_ZERO, GL_ONE_MINUS_SRC_COLOR); // revealge blend target
				glBlendEquation(GL_FUNC_ADD);

				rendering.render();
				rendering.deleteVertexData();
				glMemoryBarrier(-1);

				WBTransResolveShader shader = WBTransResolveShader.INSTANCE;
				shader.reveal.tex(revealage);
				shader.accum.tex(accum);
				shader.vert().vec3f(0, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 0, 0);
				shader.vert().vec3f(1, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 1, 0);
				shader.render();
				shader.deleteVertexData();

				try {
					Thread.sleep(100);
				} catch(InterruptedException e) {
					e.printStackTrace();
				}
			}, 10);

			return null;
		});
	}

	public static int allocateTexture(int width, int height, int storageType) {
		int tex = glGenTextures();
		int type = DataType.TEXTURE_2D.elementType;
		glBindTexture(type, tex);
		glTexStorage2D(type, 1, storageType, width, height);
		return tex;
	}
}
