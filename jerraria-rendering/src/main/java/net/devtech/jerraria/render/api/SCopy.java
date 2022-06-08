package net.devtech.jerraria.render.api;

/**
 * The method in which to copy the contents of the previous shader object.
 */
public enum SCopy {
	/**
	 * Copy uniforms but not vertex data
	 */
	PRESERVE_UNIFORMS(true, false),
	/**
	 * Copy vertex data but not uniforms
	 */
	PRESERVE_VERTEX_DATA(false, true),
	/**
	 * Copy vertex data and uniforms
	 */
	PRESERVE_BOTH(true, true),
	/**
	 * Copy neither
	 */
	PRESERVE_NEITHER(false, false);

	// todo more granular copying

	public final boolean preserveUniforms, preserveVertexData;

	SCopy(boolean uniforms, boolean data) {
		this.preserveUniforms = uniforms;
		this.preserveVertexData = data;
	}
}
