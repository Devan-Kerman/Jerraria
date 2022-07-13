package rendering;

import static org.lwjgl.opengl.GL42.GL_ALWAYS;
import static org.lwjgl.opengl.GL42.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL42.GL_R32F;
import static org.lwjgl.opengl.GL42.GL_RGBA32F;
import static org.lwjgl.opengl.GL42.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL42.glBindTexture;
import static org.lwjgl.opengl.GL42.glGenTextures;
import static org.lwjgl.opengl.GL42.glMemoryBarrier;
import static org.lwjgl.opengl.GL42.glTexStorage2D;

import net.devtech.jerraria.render.api.GLStateBuilder;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.element.AutoStrat;
import net.devtech.jerraria.render.api.translucency.TranslucentShaderType;
import net.devtech.jerraria.render.internal.shaders.WBTransResolveShader;
import net.devtech.jerraria.util.math.Mat2x3f;

public class SinglePassWeightedBlendedTranslucencyRendering {
	static {
		System.load("C:\\Program Files\\RenderDoc\\renderdoc.dll");
	}

	public static void main(String[] args) {
		Bootstrap.startClient(args, () -> {
			RenderThread.addRenderStage(() -> {
				int revealage = allocateTexture(800, 600, GL_R32F); // doesn't need to get cleared
				int accum = allocateTexture(800, 600, GL_RGBA32F);
				TestTranslucentShader rendering = TestTranslucentShader.INSTANCE;
				rendering.singlePassWeighted.accum.tex(accum);
				rendering.singlePassWeighted.reveal.tex(revealage);

				Mat2x3f identity = new Mat2x3f();

				int[] colors = {0xFF0000, 0xFFFF00, 0x00FF00, 0x00FFFF,
				                0x0000FF, 0xFF00FF, 0xFFFFFF, 0xAAAAAA};
				for(float i = 0; i < 1; i+=.125) {
					rendering.square(identity, i/2f, i/2f, .5f, .5f, -i, colors[(int) (i * 8)]);
				}

				// render to translucency buffer
				//GLContextState.DEPTH_TEST.set(true);
				//GLContextState.DEPTH_FUNC.set(GL_LESS);
				//GLContextState.DEPTH_MASK.set(false);
				//GLContextState.BLEND.set(true);
				//GLContextState.BLEND_STATE_IS[0].set(GL_ONE, GL_ONE);
				//GLContextState.BLEND_STATE_IS[1].set(GL_ZERO, GL_ONE_MINUS_SRC_COLOR);
				//GLContextState.BLEND_EQUATION.set(GL_FUNC_ADD);

				rendering.draw(TranslucentShaderType.SINGLE_PASS.defaultState);
				glMemoryBarrier(-1);

				WBTransResolveShader shader = WBTransResolveShader.INSTANCE;
				shader.reveal.tex(revealage);
				shader.accum.tex(accum);
				shader.strategy(AutoStrat.QUADS);
				shader.vert().vec3f(0, 0, 0);
				shader.vert().vec3f(0, 1, 0);
				shader.vert().vec3f(1, 1, 0);
				shader.vert().vec3f(1, 0, 0);
				//GLContextState.DEPTH_FUNC.set(GL_ALWAYS);
				//GLContextState.BLEND.set(true);
				//GLContextState.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
				shader.draw(GLStateBuilder
					.builder()
					.depthFunc(GL_ALWAYS)
					.blend(true)
					.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA)
					.build());

				try {
					Thread.sleep(1000);
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
