package net.devtech.jerraria.render.internal;

import static org.lwjgl.opengl.GL33.*;

import java.lang.ref.Cleaner;
import java.nio.ByteBuffer;
import java.util.function.IntConsumer;

import net.devtech.jerraria.render.internal.state.GLContextState;
import net.devtech.jerraria.util.math.JMath;

// todo lazy copy for translucency api!
public final class BufferObjectBuilder extends ByteBufferGlDataBuf {
	private final IntConsumer binder;
	private final int target;
	private final int componentLength;
	private Cleaner.Cleanable glIdRelease;
	private int objectIndex, storedCount, bufferLength, glId;
	private ByteBuffer store;

	public BufferObjectBuilder(BufferObjectBuilder builder) {
		this(builder, builder.totalCount());
	}

	public BufferObjectBuilder(BufferObjectBuilder builder, int copyCount) {
		this(builder.binder, builder.target, builder.componentLength, JMath.nearestPowerOf2(copyCount));
		if(builder.totalCount() < copyCount) {
			throw new IllegalArgumentException("totalCount is " + builder.totalCount() + " but requested to copy " + copyCount + " elements!");
		}
		this.copyFrom(0, builder, 0, copyCount);
	}

	public BufferObjectBuilder(IntConsumer binder, int target, int componentLength, int toStoreCount) {
		this.binder = binder;
		this.target = target;
		this.componentLength = componentLength;
		this.store = BufferBuilder.allocateBuffer(toStoreCount * componentLength);
	}

	/**
	 * uniforms are bound to global state so we use this
	 */
	public static BufferObjectBuilder uniform(int uniformLength) {
		return new BufferObjectBuilder(GLContextState.UNIFORM_BUFFER::bindBuffer,
			GLContextState.UNIFORM_BUFFER.type,
			uniformLength,
			1024
		);
	}

	/**
	 * VAOs are not bound to global state, and we create a new one for each "shader object" anyways
	 */
	public static BufferObjectBuilder vaoBound(int type, int componentLength) {
		return new BufferObjectBuilder(i -> glBindBuffer(type, i), type, componentLength, 1024);
	}

	public void appendFrom(BufferObjectBuilder builder, int off, int len) {
		this.copyFrom(this.totalCount(), builder, off, len);
	}

	public void copyFrom(int index, BufferObjectBuilder src, int off, int objects) {
		if(objects > 0) {
			if(src != this) {
				src.upload(false);
			}
			this.resize((index + objects) * this.componentLength);
			this.upload(false); // flush

			int read = GL_COPY_WRITE_BUFFER;
			if(src != this) {
				glBindBuffer(GL_COPY_READ_BUFFER, src.glId);
				read = GL_COPY_READ_BUFFER;
			}
			glBindBuffer(GL_COPY_WRITE_BUFFER, this.glId);
			glCopyBufferSubData(read,
				GL_COPY_WRITE_BUFFER,
				(long) off * this.componentLength,
				(long) index * this.componentLength,
				(long) objects * this.componentLength
			);

			this.objectIndex += objects;
		}
	}

	public void copyAttribute(int objectIndex, int byteOffset, int byteLen, BufferObjectBuilder builder, int offset) {
		if(builder != this) {
			builder.upload(false);
		}
		this.resize((objectIndex+1) * this.componentLength);
		this.upload(false); // flush

		int read = GL_COPY_WRITE_BUFFER;
		if(builder != this) {
			glBindBuffer(GL_COPY_READ_BUFFER, builder.glId);
			read = GL_COPY_READ_BUFFER;
		}
		glBindBuffer(GL_COPY_WRITE_BUFFER, this.glId);
		glCopyBufferSubData(read,
			GL_COPY_WRITE_BUFFER,
			(long) offset * this.componentLength + byteOffset,
			(long) objectIndex * this.componentLength + byteOffset,
			byteLen
		);
	}

	public void reset() {
		this.objectIndex = 0;
		this.storedCount = 0;
	}

	@SuppressWarnings("IntegerMultiplicationImplicitCastToLong")
	public long read() {
		GLContextState.UNIFORM_BUFFER.bindBuffer(this.glId);
		int[] buf = new int[1];
		glGetBufferSubData(GL_UNIFORM_BUFFER, this.objectIndex*this.componentLength, buf);
		return buf[0] & 0xFFFFFFFFL;
	}

	public boolean upload(boolean forceBind) {
		boolean genned = false;
		if(this.storedCount > 0) {
			int newBufferLen = (this.objectIndex + this.storedCount) * this.componentLength;
			long bufferObjectLen = (long) this.objectIndex * this.componentLength;
			if(newBufferLen > this.bufferLength || this.glId == 0) {
				this.resize(newBufferLen);
				genned = true;
			} else {
				this.binder.accept(this.glId);
			}

			this.store.position(0);
			this.store.limit(this.storedCount * this.componentLength);
			glBufferSubData(this.target, bufferObjectLen, this.store);
			this.store.clear();
			this.objectIndex += this.storedCount;
			this.storedCount = 0;
		} else if(forceBind) {
			this.binder.accept(this.glId);
		}
		return genned;
	}

	private void resize(int newBufferLen) {
		int alloc = JMath.nearestPowerOf2(newBufferLen + 1024);
		int old = this.glId;
		this.binder.accept(this.genBufferObject());
		glBufferData(this.target, alloc, GL_STATIC_DRAW);
		int bufferObjectLen = this.objectIndex * this.componentLength;
		if(bufferObjectLen > 0) {
			glBindBuffer(GL_COPY_READ_BUFFER, old);
			glCopyBufferSubData(GL_COPY_READ_BUFFER, this.target, 0, 0, bufferObjectLen);
		}
		this.bufferLength = alloc;
	}

	public int getOrGenId() {
		return this.glId;
	}

	public int totalCount() {
		return this.objectIndex + this.storedCount;
	}

	public void offset(int byteOffset) {
		ByteBuffer store = this.store;
		store.position(this.storedCount * this.componentLength + byteOffset);
	}

	public void index(int count) {
		if(this.objectIndex != count) {
			this.upload(false); // flush existing
			this.objectIndex = count;
		}
	}

	public void next() {
		this.storedCount++;
		int neededSize = this.storedCount * this.componentLength;
		if(this.store.capacity() < neededSize) {
			ByteBuffer buffer = BufferBuilder.allocateBuffer(JMath.nearestPowerOf2(neededSize + 1024));
			buffer.put(0, this.store, 0, this.storedCount * this.componentLength);
			this.store = buffer;
		}
	}

	private int genBufferObject() {
		GLReclamation.reclaimBuffers();
		int glId = this.glId = glGenBuffers();
		Cleaner.Cleanable register = GLReclamation.manageBuffer(this, glId);
		Cleaner.Cleanable release = this.glIdRelease;
		if(release != null) {
			release.clean();
			// do not immediately reclaim, release just pushes it into a queue
		}
		this.glIdRelease = register;
		return glId;
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.store;
	}
}
