package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.internal.DataType;
import net.devtech.jerraria.render.internal.GlData;
import net.devtech.jerraria.render.textures.Texture;

/**
 * A gl texture value, these can only be used as uniforms. They are attached as a sampler2d
 */
public class Tex<N extends GlValue<?>> extends AbstractGlValue<N> implements GlValue.Uniform {
	protected Tex(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	/**
	 * A 2d texture vertex attribute or uniform
	 *
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 */
	public static <N extends GlValue<?>> Type<Tex<N>> tex2d(String name) {
		return simple(Tex::new, DataType.TEXTURE_2D, name);
	}

	public N tex(int textureId) {
		this.data.element(this.element).i(textureId);
		return this.getNext();
	}

	public N atlas(Texture texture) {
		return this.tex(texture.getGlId());
	}
}
