package net.devtech.jerraria.render;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.util.Validate;

import net.minecraft.client.render.VertexFormat;

/**
 * Utility class for gaining access to protected RenderLayer methods
 */
public abstract class RenderLayers extends net.minecraft.client.render.RenderLayer {
	public static final MethodHandle HANDLE;

	static {
		try {
			MethodHandles.Lookup lookup = MethodHandles.lookup();
			MethodHandles.Lookup priv = MethodHandles.privateLookupIn(net.minecraft.client.render.RenderLayer.class,
				lookup
			);
			MethodType type = MethodType.methodType(net.minecraft.client.render.RenderLayer.getSolid().getClass(),
				String.class,
				VertexFormat.class,
				VertexFormat.DrawMode.class,
				int.class,
				boolean.class,
				boolean.class,
				MultiPhaseParameters.class
			);
			HANDLE = priv.findStatic(net.minecraft.client.render.RenderLayer.class, "of", type);
		} catch(ReflectiveOperationException e) {
			throw Validate.rethrow(e);
		}
	}

	public static net.minecraft.client.render.RenderLayer.MultiPhaseParameters.Builder builder() {
		return MultiPhaseParameters.builder();
	}

	public static net.minecraft.client.render.RenderLayer of(
		String name,
		VertexFormat vertexFormat,
		VertexFormat.DrawMode drawMode,
		int expectedBufferSize,
		boolean hasCrumbling,
		boolean translucent,
		MultiPhaseParameters phases) {
		try {
			return (net.minecraft.client.render.RenderLayer) HANDLE.invoke(name,
				vertexFormat,
				drawMode,
				expectedBufferSize,
				hasCrumbling,
				translucent,
				phases
			);
		} catch(Throwable e) {
			throw Validate.rethrow(e);
		}
	}

	public static <T extends MinecraftShader<?>> RenderLayer<T> renderLayer(
		T shader,
		String name,
		VertexFormat vertexFormat,
		VertexFormat.DrawMode drawMode,
		int expectedBufferSize,
		boolean hasCrumbling,
		boolean translucent,
		MultiPhaseParameters.Builder phases,
		boolean affectsOutline) {
		return new RenderLayer<>(shader,
			of(name,
				vertexFormat,
				drawMode,
				expectedBufferSize,
				hasCrumbling,
				translucent,
				phases.shader(new Shader(shader::getMinecraftShader)).build(affectsOutline)
			)
		) {
			@Override
			public <S extends MinecraftShader<?>> RenderLayer<S> withShader(S shader) {
				return renderLayer(shader,
					name,
					vertexFormat,
					drawMode,
					expectedBufferSize,
					hasCrumbling,
					translucent,
					phases,
					affectsOutline
				);
			}
		};
	}

	protected RenderLayers() {
		super(null, null, null, 0, false, false, null, null);
	}
}

