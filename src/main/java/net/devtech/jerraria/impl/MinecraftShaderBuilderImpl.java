package net.devtech.jerraria.impl;

import static net.minecraft.client.render.VertexFormats.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

import com.google.common.collect.ImmutableMap;
import net.devtech.jerraria.render.MinecraftShader;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.VFBuilder;
import net.devtech.jerraria.render.internal.VFBuilderImpl;

import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormatElement;

public class MinecraftShaderBuilderImpl<T extends GlValue<?>> implements MinecraftShader.Builder<T> {
	private static final Map<VertexFormat, VertexFormat> CACHED_FORMATS = new ConcurrentHashMap<>();
	static {
		CACHED_FORMATS.put(BLIT_SCREEN, BLIT_SCREEN);
		CACHED_FORMATS.put(POSITION_COLOR_TEXTURE_LIGHT_NORMAL, POSITION_COLOR_TEXTURE_LIGHT_NORMAL);
		CACHED_FORMATS.put(POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL, POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
		CACHED_FORMATS.put(POSITION_TEXTURE_COLOR_LIGHT, POSITION_TEXTURE_COLOR_LIGHT);
		CACHED_FORMATS.put(POSITION, POSITION);
		CACHED_FORMATS.put(POSITION_COLOR, POSITION_COLOR);
		CACHED_FORMATS.put(LINES, LINES);
		CACHED_FORMATS.put(POSITION_COLOR_LIGHT, POSITION_COLOR_LIGHT);
		CACHED_FORMATS.put(POSITION_TEXTURE, POSITION_TEXTURE);
		CACHED_FORMATS.put(POSITION_COLOR_TEXTURE, POSITION_COLOR_TEXTURE);
		CACHED_FORMATS.put(POSITION_TEXTURE_COLOR, POSITION_TEXTURE_COLOR);
		CACHED_FORMATS.put(POSITION_COLOR_TEXTURE_LIGHT, POSITION_COLOR_TEXTURE_LIGHT);
		CACHED_FORMATS.put(POSITION_TEXTURE_LIGHT_COLOR, POSITION_TEXTURE_LIGHT_COLOR);
		CACHED_FORMATS.put(POSITION_TEXTURE_COLOR_NORMAL, POSITION_TEXTURE_COLOR_NORMAL);
	}

	final ImmutableMap<String, VertexFormatElement> elements;
	final VFBuilder<T> builder;

	public MinecraftShaderBuilderImpl(ImmutableMap<String, VertexFormatElement> elements, VFBuilder<T> builder) {
		this.elements = elements;
		this.builder = builder;
	}

	public MinecraftShaderBuilderImpl() {
		this(ImmutableMap.of(), new VFBuilderImpl<>());
	}

	@Override
	public <N extends GlValue<T> & GlValue.Attribute> MinecraftShader.Builder<N> add(MinecraftShader.Type<N> type) {
		return new MinecraftShaderBuilderImpl<>(ImmutableMap.<String, VertexFormatElement>builder()
			.putAll(this.elements)
			.put(type.name(), type.element())
			.build(), this.builder.add(type.type()));
	}

	@Override
	public VertexFormat getOrCreateVertexFormat() {
		VertexFormat format = new VertexFormat(this.elements);
		return CACHED_FORMATS.computeIfAbsent(format, Function.identity());
	}

	@Override
	public VFBuilder<T> toVertexAttributeBuilder() {
		return this.builder;
	}
}
