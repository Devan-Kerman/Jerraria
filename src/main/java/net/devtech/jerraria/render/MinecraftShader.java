package net.devtech.jerraria.render;

import java.util.HashMap;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import net.devtech.jerraria.impl.MinecraftShaderBuilderImpl;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.SCopy;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.api.base.GlData;
import net.devtech.jerraria.render.api.types.Color;
import net.devtech.jerraria.render.api.types.End;
import net.devtech.jerraria.render.api.types.Mat3;
import net.devtech.jerraria.render.api.types.Mat4;
import net.devtech.jerraria.render.api.types.Normal;
import net.devtech.jerraria.render.api.types.Overlay;
import net.devtech.jerraria.render.api.types.V;
import net.devtech.jerraria.render.api.types.Vec2;
import net.devtech.jerraria.render.api.types.Vec3;
import net.devtech.jerraria.render.api.types.Vec4;
import net.devtech.jerraria.render.internal.renderhandler.RenderHandler;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gl.GlBlendState;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;
import net.minecraft.client.render.VertexFormats;

public abstract class MinecraftShader<T extends GlValue<?> & GlValue.Attribute> extends Shader<T> {
	public final VertexFormat format;
	final int[] reloadCounter;
	int reload = -1;
	net.minecraft.client.render.Shader minecraftShader;

	public static <T extends Shader<?>> T createMinecraft(Id id, Copier<T> copyFunction, Initializer<T> initializer) {
		return ShaderImpl.createShader(id, copyFunction, (builder, context) -> {
			Validate.isTrue(builder.isEmpty(), "builder must be empty!");
			return initializer.create(new MinecraftShaderBuilderImpl<>(), context);
		}, RenderHandler.INSTANCE);
	}

	public final Mat4.x4<?> modelViewMatrix = this.uni(Mat4.<End>mat4("ModelViewMat").optional());
	public final Mat4.x4<?> projectionMatrix = this.uni(Mat4.<End>mat4("ProjMat").optional());
	public final Mat3.x3<?> viewRotationMatrix = this.uni(Mat3.<End>mat3("IViewRotMat").optional());
	public final Mat4.x4<?> textureMatrix = this.uni(Mat4.<End>mat4("TextureMat").optional());
	public final Vec2.F<?> screenSize = this.uni(Vec2.<End>f("ScreenSize").optional());
	public final Vec3.F<?> chunkOffset = this.uni(Vec3.<End>f("ChunkOffset").optional());
	public final V.I<?> fogShape = this.uni(V.<End>i("FogShape").optional());
	public final V.F<?> fogStart = this.uni(V.<End>f("FogStart").optional());
	public final V.F<?> fogEnd = this.uni(V.<End>f("FogEnd").optional());
	public final Vec4.F<?> fogColor = this.uni(Vec4.<End>f("FogColor").optional());
	public final Map<String, ElementImpl> elements;
	public final VFBuilder<T> builder;

	protected MinecraftShader(Builder<T> builder, Object context) {
		super(builder.toVertexAttributeBuilder(), context);
		this.builder = builder.toVertexAttributeBuilder();
		this.format = builder.getOrCreateVertexFormat();
		this.reloadCounter = new int[] {0};
		Map<String, ElementImpl> elements = new HashMap<>();
		ImmutableList<VertexFormatElement> formatElements = format.getElements();
		for(int i = 0; i < formatElements.size(); i++) {
			VertexFormatElement element = formatElements.get(i);
			elements.put(format.getAttributeNames().get(i), new ElementImpl(element, i));
		}
		this.elements = Map.copyOf(elements);
	}

	public MinecraftShader(Shader<T> shader, SCopy method) {
		super(shader, method);
		MinecraftShader<T> mcShader = (MinecraftShader<T>) shader;
		this.format = mcShader.format;
		this.reloadCounter = mcShader.reloadCounter;
		this.elements = mcShader.elements;
		this.builder = mcShader.builder;
	}

	public static <N extends GlValue<?>> Type<Vec3.F<N>> pos(String name) {
		return new Type<>(Vec3.f(name), VertexFormats.POSITION_ELEMENT, name);
	}

	public static <N extends GlValue<?>> Type<Color.ARGB<N>> color(String name) {
		return new Type<>(Color.argb(name), VertexFormats.COLOR_ELEMENT, name);
	}

	public static <N extends GlValue<?>> Type<Vec2.F<N>> tex(String name) {
		return new Type<>(Vec2.f(name), VertexFormats.TEXTURE_ELEMENT, name);
	}

	public static <N extends GlValue<?>> Type<Overlay<N>> overlay(String name) {
		return new Type<>(Overlay.overlay(name), VertexFormats.OVERLAY_ELEMENT, name);
	}

	public static <N extends GlValue<?>> Type<Overlay<N>> light(String name) {
		return new Type<>(Overlay.overlay(name), VertexFormats.LIGHT_ELEMENT, name);
	}

	public static <N extends GlValue<?>> Type<Normal<N>> normal(String name) {
		return new Type<>(Normal.normal(name), VertexFormats.NORMAL_ELEMENT, name);
	}

	public net.minecraft.client.render.Shader getMinecraftShader() {
		if(this.reloadCounter[0] != this.reload) {
			this.reload = this.reloadCounter[0];
			this.minecraftShader = MinecraftShaderLoader.createShader(this);
		}
		return this.minecraftShader;
	}

	/**
	 * @see RenderLayers#renderLayer(MinecraftShader, String, VertexFormat, VertexFormat.DrawMode, int, boolean, boolean, RenderLayer.MultiPhaseParameters.Builder, boolean)
	 */
	public VertexConsumerImpl<T> provider(VertexConsumer consumer) {
		return new VertexConsumerImpl<>(this, consumer);
	}

	public interface Builder<T extends GlValue<?>> {
		<N extends GlValue<T> & GlValue.Attribute> Builder<N> add(Type<N> type);

		VertexFormat getOrCreateVertexFormat();

		VFBuilder<T> toVertexAttributeBuilder();
	}

	public interface Initializer<T extends Shader<?>> {
		T create(Builder<End> builder, Object context);
	}

	public record Type<N extends GlValue<?>>(GlValue.Type<N> type, VertexFormatElement element, String name) {
	}

	record ElementImpl(VertexFormatElement element, int index) implements GlData.Element {
	}

	@Nullable
	protected GlBlendState defaultBlendState() {
		return null;
	}

	@Override
	public void reload() {
		super.reload();
		this.reloadCounter[0]++;
	}
}
