package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.AbstractGlValue;
import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.basic.DataType;
import net.devtech.jerraria.render.api.basic.GlData;

public class FrameOut extends AbstractGlValue<End> implements GlValue.Output {
	public static Type<FrameOut> out2d(String name) {
		return out(name, DataType.TEXTURE_2D);
	}

	/**
	 * @see Shader#addOutput(String, DataType)
	 * @param name the name of the output fragment
	 * @param imageType the image type to write to
	 */
	public static Type<FrameOut> out(String name, DataType imageType) {
		if(DataType.VALID_OUTPUTS.contains(imageType)) {
			return simple(FrameOut::new, imageType, name);
		} else {
			throw new UnsupportedOperationException("Output image types are unrestricted, use one of " + DataType.VALID_OUTPUTS);
		}
	}

	protected FrameOut(GlData data, GlValue next, String name) {
		super(data, next, name);
	}

	public void tex(int texture) {
		this.data.element(this.element).i(texture);
	}
}
