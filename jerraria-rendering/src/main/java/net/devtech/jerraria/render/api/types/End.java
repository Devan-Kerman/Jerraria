package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;

/**
 * Denotes the end of a vertex attribute offsets.
 */
public class End extends GlValue<End> implements GlValue.Attribute {
	public End() {
		super(null, null);
	}
}
