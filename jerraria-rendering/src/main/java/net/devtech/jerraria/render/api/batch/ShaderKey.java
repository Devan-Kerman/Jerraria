package net.devtech.jerraria.render.api.batch;

import net.devtech.jerraria.render.api.Shader;

public abstract class ShaderKey<T extends Shader<?>> {
	protected abstract void drawKeep(T batch);

	/**
	 * @return True if the given shader key is the same as this key, if this key is some public static constant, then reference equality should be enough
	 */
	@Override
	public abstract boolean equals(Object obj);

	@Override
	public abstract int hashCode();

	protected final int identityHashCode() {
		return super.hashCode();
	}

}
