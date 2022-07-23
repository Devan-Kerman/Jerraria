package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.base.DataType;
import net.devtech.jerraria.render.api.base.GlData;

import net.minecraft.client.render.LightmapTextureManager;

public class Overlay<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Attribute, GlValue.Uniform {
	public static <N extends GlValue<?>> Type<Overlay<N>> overlay(String name) {
		return overlay(name, null);
	}

	public static <N extends GlValue<?>> Type<Overlay<N>> overlay(String name, String groupName) {
		return simple(Overlay::new, DataType.NORMALIZED_F16_VEC2, name, groupName);
	}

	protected Overlay(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	public N overlay(short u, short v) {
		this.data.element(this.element).s(u).s(v);
		return this.getNext();
	}

	public N overlay(int uv) {
		return this.overlay((short) (uv & 0xFFFF), (short) (uv >> 16 & 0xFFFF));
	}

	public N maxBlockLight() {
		return this.overlay(LightmapTextureManager.MAX_BLOCK_LIGHT_COORDINATE);
	}

	public N maxSkyLight() {
		return this.overlay(LightmapTextureManager.MAX_SKY_LIGHT_COORDINATE);
	}

	public N maxBright() {
		return this.overlay(LightmapTextureManager.MAX_LIGHT_COORDINATE);
	}
}
