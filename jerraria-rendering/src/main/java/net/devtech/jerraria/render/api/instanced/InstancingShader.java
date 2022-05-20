package net.devtech.jerraria.render.api.instanced;

public interface InstancingShader {
	/**
	 * @return The maximum number of instances once instance of this shader can render at once
	 */
	int getCapacity();
}
