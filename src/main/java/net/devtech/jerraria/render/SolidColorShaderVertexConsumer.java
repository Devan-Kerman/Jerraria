package net.devtech.jerraria.render;

import net.devtech.jerraria.gui.api.shaders.SolidColorShader;

import net.minecraft.client.render.VertexConsumer;

public class SolidColorShaderVertexConsumer implements VertexConsumer {
	final SolidColorShader shader;
	float x, y, z;
	int argb;

	public SolidColorShaderVertexConsumer(SolidColorShader shader) {
		this.shader = shader;
	}

	@Override
	public VertexConsumer vertex(double x, double y, double z) {
		this.x = (float) x;
		this.y = (float) y;
		this.z = (float) z;
		return this;
	}

	@Override
	public VertexConsumer color(int red, int green, int blue, int alpha) {
		this.argb = ((alpha & 0xFF) << 24) | ((red & 0xFF) << 16) << ((green & 0xFF) << 8) | (blue & 0xFF);
		return this;
	}

	@Override
	public VertexConsumer texture(float u, float v) {
		return this;
	}

	@Override
	public VertexConsumer overlay(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer light(int u, int v) {
		return this;
	}

	@Override
	public VertexConsumer normal(float x, float y, float z) {
		return this;
	}

	@Override
	public void next() {
		this.shader.vert().vec3f(this.x, this.y, this.z).argb(this.argb);
	}

	@Override
	public void fixedColor(int red, int green, int blue, int alpha) {
	}

	@Override
	public void unfixColor() {
	}
}
