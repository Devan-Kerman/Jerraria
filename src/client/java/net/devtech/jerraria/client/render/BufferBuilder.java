package net.devtech.jerraria.client.render;

import static org.lwjgl.opengl.GL20.*;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import org.lwjgl.opengl.GL20;
import org.lwjgl.system.MemoryUtil;

public final class BufferBuilder {
	ByteBuffer buffer;
	int glId;

	public BufferBuilder() {
		this.buffer = allocateBuffer(1024);
	}

	public BufferBuilder(int expectedSize) {
		// compute the next highest power of 2 of 32-bit v
		long v = expectedSize & 0xFFFFFFFFL;
		v--;
		v |= v >> 1;
		v |= v >> 2;
		v |= v >> 4;
		v |= v >> 8;
		v |= v >> 16;
		v++;
		this.buffer = allocateBuffer((int) v);
	}

	public void bind(boolean reset) {
		if(this.glId == MemoryUtil.NULL) {
			int glId = this.glId = glGenBuffers();
			glBindBuffer(GL_ARRAY_BUFFER, glId);
		}
		ByteBuffer buffer = this.buffer;
		if(reset) {
			buffer.rewind();
			glBufferData(GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
		} else {
			int pos = buffer.position();
			buffer.rewind();
			glBufferData(GL_ARRAY_BUFFER, buffer, GL20.GL_STATIC_DRAW);
			buffer.position(pos);
		}
	}

	public void reset() {
		this.buffer.rewind();
	}

	public BufferBuilder bool(boolean bool) {
		this.ensureCapacity(1).put((byte) (bool ? 1 : 0));
		return this;
	}

	public BufferBuilder i32(int val) {
		this.ensureCapacity(4).putInt(val);
		return this;
	}

	public BufferBuilder u32(int val) {
		this.ensureCapacity(4).putInt(val);
		return this;
	}

	public BufferBuilder f32(float val) {
		this.ensureCapacity(4).putFloat(val);
		return this;
	}

	public BufferBuilder f64(double val) {
		this.ensureCapacity(8).putDouble(val);
		return this;
	}

	public BufferBuilder bvec2(boolean x, boolean y) {
		this.bool(x);
		this.bool(y);
		return this;
	}

	public BufferBuilder bvec3(boolean x, boolean y, boolean z) {
		this.bool(x);
		this.bool(y);
		this.bool(z);
		return this;
	}

	public BufferBuilder bvec4(boolean x, boolean y, boolean z, boolean w) {
		this.bool(x);
		this.bool(y);
		this.bool(z);
		this.bool(w);
		return this;
	}

	public BufferBuilder uvec2(int x, int y) {
		this.u32(x);
		this.u32(y);
		return this;
	}

	public BufferBuilder uvec3(int x, int y, int z) {
		this.u32(x);
		this.u32(y);
		this.u32(z);
		return this;
	}

	public BufferBuilder uvec4(int x, int y, int z, int w) {
		this.u32(x);
		this.u32(y);
		this.u32(z);
		this.u32(w);
		return this;
	}

	public BufferBuilder ivec2(int x, int y) {
		this.i32(x);
		this.i32(y);
		return this;
	}

	public BufferBuilder ivec3(int x, int y, int z) {
		this.i32(x);
		this.i32(y);
		this.i32(z);
		return this;
	}

	public BufferBuilder ivec4(int x, int y, int z, int w) {
		this.i32(x);
		this.i32(y);
		this.i32(z);
		this.i32(w);
		return this;
	}

	public BufferBuilder vec2(float x, float y) {
		this.f32(x);
		this.f32(y);
		return this;
	}

	public BufferBuilder vec3(float x, float y, float z) {
		this.f32(x);
		this.f32(y);
		this.f32(z);
		return this;
	}

	public BufferBuilder vec4(float x, float y, float z, float w) {
		this.fput4(x, y, z, w);
		return this;
	}

	public BufferBuilder dvec2(double x, double y) {
		this.f64(x);
		this.f64(y);
		return this;
	}

	public BufferBuilder dvec3(double x, double y, double z) {
		this.f64(x);
		this.f64(y);
		this.f64(z);
		return this;
	}

	public BufferBuilder dvec4(double x, double y, double z, double w) {
		this.dput4(x, y, z, w);
		return this;
	}

	public BufferBuilder mat2(float a00, float a01, float a10, float a11) {
		this.fput4(a00, a01, a10, a11);
		return this;
	}

	public BufferBuilder mat2x3(float a00, float a01, float a02, float a10, float a11, float a12) {
		this.fput4(
			a00,
			a01,
			a02,
			a10);
		this.f32(a11);
		this.f32(a12);
		return this;
	}

	public BufferBuilder mat2x4(float a00,
		float a01,
		float a02,
		float a03,
		float a10,
		float a11,
		float a12,
		float a13) {
		this.fput8(a00, a01, a02, a03, a10, a11, a12, a13);
		return this;
	}

	public BufferBuilder mat3x2(float a00, float a01, float a10, float a11, float a20, float a21) {
		this.fput4(
			a00,
			a01,
			a10,
			a11);
		this.f32(a20);
		this.f32(a21);
		return this;
	}

	public BufferBuilder mat3(float a00,
		float a01,
		float a02,
		float a10,
		float a11,
		float a12,
		float a20,
		float a21,
		float a22) {
		this.fput8(a00, a01, a02, a10, a11, a12, a20, a21);
		this.f32(a22);
		return this;
	}

	public BufferBuilder mat3x4(float a00,
		float a01,
		float a02,
		float a03,
		float a10,
		float a11,
		float a12,
		float a13,
		float a20,
		float a21,
		float a22,
		float a23) {
		this.fput8(a00, a01, a02, a03, a10, a11, a12, a13);
		this.fput4(a20, a21, a22, a23);
		return this;
	}

	public BufferBuilder mat4x2(float a00,
		float a01,
		float a10,
		float a11,
		float a20,
		float a21,
		float a30,
		float a31) {
		this.fput8(a00, a01, a10, a11, a20, a21, a30, a31);
		return this;
	}

	public BufferBuilder mat4x3(float a00,
		float a01,
		float a02,
		float a10,
		float a11,
		float a12,
		float a20,
		float a21,
		float a22,
		float a30,
		float a31,
		float a32) {
		this.fput8(a00, a01, a02, a10, a11, a12, a20, a21);
		this.fput4(a22, a30, a31, a32);
		return this;
	}

	public BufferBuilder mat4(float a00,
		float a01,
		float a02,
		float a03,
		float a10,
		float a11,
		float a12,
		float a13,
		float a20,
		float a21,
		float a22,
		float a23,
		float a30,
		float a31,
		float a32,
		float a33) {
		this.fput8(a00, a01, a02, a03, a10, a11, a12, a13);
		this.fput8(a20, a21, a22, a23, a30, a31, a32, a33);
		return this;
	}

	public BufferBuilder dmat2(double a00, double a01, double a10, double a11) {
		this.dput4(a00, a01, a10, a11);
		return this;
	}

	public BufferBuilder dmat2x3(double a00, double a01, double a02, double a10, double a11, double a12) {
		this.dput4(
			a00,
			a01,
			a02,
			a10);
		this.f64(a11);
		this.f64(a12);
		return this;
	}

	public BufferBuilder dmat2x4(double a00,
		double a01,
		double a02,
		double a03,
		double a10,
		double a11,
		double a12,
		double a13) {
		this.dput8(a00, a01, a02, a03, a10, a11, a12, a13);
		return this;
	}

	public BufferBuilder dmat3x2(double a00, double a01, double a10, double a11, double a20, double a21) {
		this.dput4(
			a00,
			a01,
			a10,
			a11);
		this.f64(a20);
		this.f64(a21);
		return this;
	}

	public BufferBuilder dmat3(double a00,
		double a01,
		double a02,
		double a10,
		double a11,
		double a12,
		double a20,
		double a21,
		double a22) {
		this.dput8(a00, a01, a02, a10, a11, a12, a20, a21);
		this.f64(a22);
		return this;
	}

	public BufferBuilder dmat3x4(double a00,
		double a01,
		double a02,
		double a03,
		double a10,
		double a11,
		double a12,
		double a13,
		double a20,
		double a21,
		double a22,
		double a23) {
		this.dput8(a00, a01, a02, a03, a10, a11, a12, a13);
		this.dput4(a20, a21, a22, a23);
		return this;
	}

	public BufferBuilder dmat4x2(double a00,
		double a01,
		double a10,
		double a11,
		double a20,
		double a21,
		double a30,
		double a31) {
		this.dput8(a00, a01, a10, a11, a20, a21, a30, a31);
		return this;
	}

	public BufferBuilder dmat4x3(double a00,
		double a01,
		double a02,
		double a10,
		double a11,
		double a12,
		double a20,
		double a21,
		double a22,
		double a30,
		double a31,
		double a32) {
		this.dput8(a00, a01, a02, a10, a11, a12, a20, a21);
		this.dput4(a22, a30, a31, a32);
		return this;
	}

	public BufferBuilder dmat4(double a00,
		double a01,
		double a02,
		double a03,
		double a10,
		double a11,
		double a12,
		double a13,
		double a20,
		double a21,
		double a22,
		double a23,
		double a30,
		double a31,
		double a32,
		double a33) {
		this.dput8(a00, a01, a02, a03, a10, a11, a12, a13);
		this.dput8(a20, a21, a22, a23, a30, a31, a32, a33);
		return this;
	}

	private static ByteBuffer allocateBuffer(int size) {
		ByteBuffer buffer = ByteBuffer.allocateDirect(size);
		buffer.order(ByteOrder.nativeOrder());
		return buffer;
	}

	private ByteBuffer ensureCapacity(int bytes) {
		ByteBuffer buffer = this.buffer;
		if(buffer.remaining() < bytes) {
			ByteBuffer old = buffer;
			int oldCount = buffer.limit();
			buffer = allocateBuffer(oldCount << 1);
			buffer.put(0, old, 0, oldCount);
			buffer.position(old.position());
			this.buffer = buffer;
		}
		return buffer;
	}

	private void dput4(double a00, double a01, double a10, double a11) {
		this.f64(a00);
		this.f64(a01);
		this.f64(a10);
		this.f64(a11);
	}

	private void dput8(double a00,
		double a01,
		double a10,
		double a11,
		double a20,
		double a21,
		double a30,
		double a31) {
		this.dput4(a00, a01, a10, a11);
		this.dput4(a20, a21, a30, a31);
	}

	private void fput4(float a00, float a01, float a10, float a11) {
		this.f32(a00);
		this.f32(a01);
		this.f32(a10);
		this.f32(a11);
	}

	private void fput8(float a00, float a01, float a02, float a03, float a10, float a11, float a12, float a13) {
		this.fput4(
			a00,
			a01,
			a02,
			a03);
		this.fput4(a10, a11, a12, a13);
	}
}
