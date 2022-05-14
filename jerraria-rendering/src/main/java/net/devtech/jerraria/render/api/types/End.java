package net.devtech.jerraria.render.api.types;

import net.devtech.jerraria.render.api.GlValue;

/**
 * Denotes the end of a vertex attribute list.
 */
public class End extends GlValue<End> {
	int vertexId;
	public End() {
		super(null, null);
	}

	/**
	 * @return The id of the newly written vertex data
	 */
	public int id() {
		return this.vertexId;
	}
}
