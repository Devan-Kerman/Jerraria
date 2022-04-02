package net.devtech.jerraria.client.render.api;

public enum SCopy {
	PRESERVE_UNIFORMS(true, false),
	PRESERVE_VERTEX_DATA(false, true),
	PRESERVE_BOTH(true, true),
	PRESERVE_NEITHER(false, false);

	// todo view-only stuff, so u can "share vertex data" for example

	public final boolean preserveUniforms, preserveVertexData;

	SCopy(boolean uniforms, boolean data) {
		this.preserveUniforms = uniforms;
		this.preserveVertexData = data;
	}
}
