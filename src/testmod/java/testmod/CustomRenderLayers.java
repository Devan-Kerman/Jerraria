package testmod;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.devtech.jerraria.util.Validate;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.RenderPhase;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public class CustomRenderLayers extends RenderLayer {
	public static final MethodHandle HANDLE;

	static {

		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandles.Lookup priv = MethodHandles.privateLookupIn(RenderLayer.class, lookup);
			MethodType type = MethodType.methodType(RenderLayer.getSolid().getClass(),
				String.class,
				VertexFormat.class,
				VertexFormat.DrawMode.class,
				int.class,
				boolean.class,
				boolean.class,
				MultiPhaseParameters.class
			);
			HANDLE = priv.findStatic(RenderLayer.class, "of", type);
		} catch(ReflectiveOperationException e) {
			throw Validate.rethrow(e);
		}
	}

	public static RenderLayer of(String name, VertexFormat vertexFormat, VertexFormat.DrawMode drawMode, int expectedBufferSize, boolean hasCrumbling, boolean translucent, MultiPhaseParameters phases) {
		try {
			return (RenderLayer) HANDLE.invoke(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, phases);
		} catch(Throwable e) {
			throw Validate.rethrow(e);
		}
	}

	public static final RenderLayer RENDER_LAYER = of("custom_render_layer",
		VertexFormats.POSITION,
		VertexFormat.DrawMode.QUADS,
		256,
		false,
		true,
		RenderLayer.MultiPhaseParameters.builder()
			.cull(RenderPhase.DISABLE_CULLING)
			.shader(new RenderPhase.Shader(TestShader.INSTANCE::getMinecraftShader))
			.build(false)
	);

	private CustomRenderLayers(
		String name,
		VertexFormat vertexFormat,
		VertexFormat.DrawMode drawMode,
		int expectedBufferSize,
		boolean hasCrumbling,
		boolean translucent,
		Runnable startAction,
		Runnable endAction) {
		super(name, vertexFormat, drawMode, expectedBufferSize, hasCrumbling, translucent, startAction, endAction);
	}
}
