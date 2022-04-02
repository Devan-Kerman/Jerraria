package net.devtech.jerraria.render.api;

import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.render.textures.Atlas;
import net.devtech.jerraria.render.textures.Texture;

public class Tex<N extends GlValue<?>> extends GlValue<N> {
	final String name;
	final GlData.Element element;

	public static <N extends GlValue<?>> GlValue.Type<Tex<N>> tex2d(String name) {
		return simple((data, next) -> new Tex<>(data, next, name), DataType.TEXTURE_2D, name);
	}

	protected Tex(GlData data, GlValue next, String name) {
		super(data, next);
		this.name = name;
		this.element = data.getElement(this.name);
	}

	public N tex(int textureId) {
		this.data.element(this.element).i(textureId);
		return this.getNext();
	}

	public N atlas(Texture texture) {
		return this.tex(texture.getGlId());
	}

	public N atlas(Atlas atlas) {
		return this.tex(atlas.glId());
	}
}
