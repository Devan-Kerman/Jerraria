package net.devtech.jerraria.client.render;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

public final class BufferBuilder {
	final ByteBuffer buffer;

	public BufferBuilder(ByteBuffer buffer) {
		this.buffer = buffer;
	}

	public BufferBuilder bool(boolean bool) {
		this.buffer.put((byte) (bool ? 1 : 0));
		return this;
	}

	public BufferBuilder i32(int val) {
		this.buffer.putInt(val);
		return this;
	}

	public BufferBuilder u32(int val) {
		this.buffer.putInt(val);
		return this;
	}

	public BufferBuilder f32(float val) {
		this.buffer.putFloat(val);
		return this;
	}

	public BufferBuilder f64(double val) {
		this.buffer.putDouble(val);
		return this;
	}

	public BufferBuilder bvec2(boolean a, boolean b) {
		this.bool(a);
		this.bool(b);
		return this;
	}

	public BufferBuilder bvec3(boolean a, boolean b, boolean c) {
		this.bool(a);
		this.bool(b);
		this.bool(c);
		return this;
	}

	public BufferBuilder bvec4(boolean a, boolean b, boolean c, boolean d) {
		this.bool(a);
		this.bool(b);
		this.bool(c);
		this.bool(d);
		return this;
	}

	public BufferBuilder uvec2(int a, int b) {
		this.u32(a);
		this.u32(b);
		return this;
	}

	public BufferBuilder uvec3(int a, int b, int c) {
		this.u32(a);
		this.u32(b);
		this.u32(c);
		return this;
	}

	public BufferBuilder uvec4(int a, int b, int c, int d) {
		this.u32(a);
		this.u32(b);
		this.u32(c);
		this.u32(d);
		return this;
	}

	public BufferBuilder ivec2(int a, int b) {
		this.i32(a);
		this.i32(b);
		return this;
	}

	public BufferBuilder ivec3(int a, int b, int c) {
		this.i32(a);
		this.i32(b);
		this.i32(c);
		return this;
	}

	public BufferBuilder ivec4(int a, int b, int c, int d) {
		this.i32(a);
		this.i32(b);
		this.i32(c);
		this.i32(d);
		return this;
	}

	public BufferBuilder vec2(float a, float b) {
		this.f32(a);
		this.f32(b);
		return this;
	}

	public BufferBuilder vec3(float a, float b, float c) {
		this.f32(a);
		this.f32(b);
		this.f32(c);
		return this;
	}

	public BufferBuilder vec4(float a, float b, float c, float d) {
		this.put4(a, b, c, d);
		return this;
	}

	public BufferBuilder dvec2(double a, double b) {
		this.f64(a);
		this.f64(b);
		return this;
	}

	public BufferBuilder dvec3(double a, double b, double c) {
		this.f64(a);
		this.f64(b);
		this.f64(c);
		return this;
	}

	public BufferBuilder dvec4(double a, double b, double c, double d) {
		this.put4(a, b, c, d);
		return this;
	}


	public BufferBuilder mat2(float a, float b, float c, float d) {
		this.put4(a, b, c, d);
		return this;
	}

	public BufferBuilder mat2x3(float a, float b, float c, float d, float e, float f) {
		this.put4(a, b, c, d);
		this.f32(e);
		this.f32(f);
		return this;
	}

	public BufferBuilder mat2x4(float a, float b, float c, float d, float e, float f, float g, float h) {
		return this.put8(a, b, c, d, e, f, g, h);
	}

	public BufferBuilder mat3x2(float a, float b, float c, float d, float e, float f) {
		this.put4(a, b, c, d);
		this.f32(e);
		this.f32(f);
		return this;
	}

	public BufferBuilder mat3(float a, float b, float c, float d, float e, float f, float g, float h, float i) {
		this.f32(a);
		return this.put8(b, c, d, e, f, g, h, i);
	}

	public BufferBuilder mat3x4(float a,
		float b,
		float c,
		float d,
		float e,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l) {
		this.put4(a, b, c, d);
		return this.put8(e, f, g, h, i, j, k, l);
	}

	public BufferBuilder mat4x2(float a, float b, float c, float d, float e, float f, float g, float h) {
		return this.put8(a, b, c, d, e, f, g, h);
	}

	public BufferBuilder mat4x3(float a,
		float b,
		float c,
		float d,
		float e,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l) {
		this.put4(a, b, c, d);
		return this.put8(e, f, g, h, i, j, k, l);
	}

	public BufferBuilder mat4(float a,
		float b,
		float c,
		float d,
		float e,
		float f,
		float g,
		float h,
		float i,
		float j,
		float k,
		float l,
		float m,
		float n,
		float o,
		float p) {
		this.put8(a, b, c, d, e, f, g, h);
		return this.put8(i, j, k, l, m, n, o, p);
	}

	public BufferBuilder dmat2(double a, double b, double c, double d) {
		this.put4(a, b, c, d);
		return this;
	}

	public BufferBuilder dmat2x3(double a, double b, double c, double d, double e, double f) {
		this.put4(a, b, c, d);
		this.f64(e);
		this.f64(f);
		return this;
	}

	public BufferBuilder dmat2x4(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h) {
		this.put8(a, b, c, d, e, f, g, h);
		return this;
	}

	public BufferBuilder dmat3x2(double a, double b, double c, double d, double e, double f) {
		this.put4(a, b, c, d);
		this.f64(e);
		this.f64(f);
		return this;
	}

	public BufferBuilder dmat3(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i) {
		this.put8(a, b, c, d, e, f, g, h);
		this.f64(i);
		return this;
	}

	public BufferBuilder dmat3x4(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		double j,
		double k,
		double l) {
		this.put8(a, b, c, d, e, f, g, h);
		this.put4(i, j, k, l);
		return this;
	}

	public BufferBuilder dmat4x2(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h) {
		this.put8(a, b, c, d, e, f, g, h);
		return this;
	}

	public BufferBuilder dmat4x3(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		double j,
		double k,
		double l) {
		this.put8(a, b, c, d, e, f, g, h);
		this.put4(i, j, k, l);
		return this;
	}

	public BufferBuilder dmat4(double a,
		double b,
		double c,
		double d,
		double e,
		double f,
		double g,
		double h,
		double i,
		double j,
		double k,
		double l,
		double m,
		double n,
		double o,
		double p) {
		this.put8(a, b, c, d, e, f, g, h);
		this.put8(i, j, k, l, m, n, o, p);
		return this;
	}

	private void put8(double i, double j, double k, double l, double m, double n, double o, double p) {
		this.put4(i, j, k, l);
		this.put4(m, n, o, p);
	}

	private void put4(double m, double n, double o, double p) {
		this.f64(m);
		this.f64(n);
		this.f64(o);
		this.f64(p);
	}


	private void put4(float a, float b, float c, float d) {
		this.f32(a);
		this.f32(b);
		this.f32(c);
		this.f32(d);
	}

	private BufferBuilder put8(float a, float b, float c, float d, float e, float f, float g, float h) {
		this.put4(a, b, c, d);
		this.put4(e, f, g, h);
		return this;
	}
}
