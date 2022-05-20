package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;
import net.devtech.jerraria.render.api.basic.ImageFormat;
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
	 * @param name the full path of the vertex attribute or uniform in the shader {@link #simple(SimpleType, DataType,
	 *    String)}
	 */
	public static <N extends GlValue<?>> Type<Tex<N>> tex2d(String name) {
		return simple(Tex::new, DataType.TEXTURE_2D, name);
	}

	/**
	 * A 2d texture vertex attribute or uniform
	 *
	 * @param name the full path of the uniform in the shader {@link #simple(SimpleType, DataType, String)}
	 * @param format the format that passed images will come in, this must be identical to the format specified in the
	 * 	shader (if it is specified). This sets how the image will be interpreted by opengl. There is some "interesting"
	 * 	(see, painful) mechanisms whereby you can use "compatible" image formats. So there would be a potential
	 * 	uses for not statically declaring the image format, however this is so full of holes, and I don't know of any
	 * 	way to add a validation mechanism, so we'll ignore it.
	 */
	public static <N extends GlValue<?>> Type<Tex<N>> img(String name, DataType type, ImageFormat format) {
		return new Simple<>(Tex::new, type, name, null, format);
	}

	public N tex(int textureId) {
		this.data.element(this.element).i(textureId);
		return this.getNext();
	}

	public N atlas(Texture texture) {
		return this.tex(texture.getGlId());
	}
}
