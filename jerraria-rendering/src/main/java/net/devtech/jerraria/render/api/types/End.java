package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;
import net.devtech.jerraria.render.api.Shader;

/**
 * Denotes the end of a vertex attribute list.
 */
public class End extends GlValue<End> implements GlValue.Attribute {
	int vertexId; // todo determine what vertices to keep and what to nuke
	public End() {
		super(null, null);
	}

	/**
	 * @return The id of the newly written vertex data. This id is only valid for a given Shader's vertex data
	 * @see Shader#copy(int)
	 */
	public int id() {
		return this.vertexId;
	}
}
