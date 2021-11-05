package net.devtech.jerraria.client.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL20;

public enum BufferUse {
	IMMUTABLE(GL20.GL_STREAM_DRAW),
	RENDERED_FREQUENTLY(GL20.GL_STATIC_DRAW),
	MUTATED_FREQUENTLY(GL20.GL_DYNAMIC_DRAW);

	public final int glFlag;

	BufferUse(int flag) {this.glFlag = flag;}
}
