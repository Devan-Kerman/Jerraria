package net.devtech.jerraria.render.api;

public interface GlStateStack extends AutoCloseable {
	static GLStateBuilder builder() {
		return GLStateBuilder.builder();
	}

	void forceReapply();

	GLStateBuilder copyToBuilder();

	@Override
	void close();

	GlStateStack copy();
}
