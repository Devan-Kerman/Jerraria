package net.devtech.jerraria.render.api;

import net.devtech.jerraria.util.SafeClosable;

public abstract class GlStateStack {
	/**
	 * <pre>{@code
	 * try(GlStateStack.builder().blend(true).apply().self) {
	 *     // do whatever
	 * }
	 * }</pre>
	 */
	public final SafeClosable self = this::close;

	public static GLStateBuilder builder() {
		return GLStateBuilder.builder();
	}

	public abstract void forceReapply();

	public abstract GLStateBuilder copyToBuilder();

	public abstract void close();

	public abstract GlStateStack copy();
}
