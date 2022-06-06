package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL46.*;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;

import net.devtech.jerraria.render.internal.ByteBufferGlDataBuf;
import net.devtech.jerraria.util.math.JMath;

public final class VertexBufferObjectBuilder extends ByteBufferGlDataBuf {
	private final IntConsumer binder;
	private final int target;
	private final int componentLength;
	List<VertexBufferObjectBuilder> copies;
	VertexBufferObjectBuilder deferredSource;
	int copyCount;
	private int objectIndex, storedCount, bufferLength, glId;
	private ByteBuffer store;

	public VertexBufferObjectBuilder(VertexBufferObjectBuilder builder) {
		this(builder, builder.totalCount());
	}

	public VertexBufferObjectBuilder(VertexBufferObjectBuilder builder, int copyCount) {
		this(builder.binder, builder.target, builder.componentLength, JMath.nearestPowerOf2(copyCount+1024));
		if(builder.totalCount() < copyCount) {
			throw new IllegalArgumentException("totalCount is " + builder.totalCount() + " but requested to copy " + copyCount + " elements!");
		}
		builder.upload(false);
		this.deferredSource = builder;
		this.copyCount = copyCount;
		if(builder.copies == null) {
			builder.copies = new ArrayList<>();
		}
		builder.copies.add(this);
	}

	public VertexBufferObjectBuilder(IntConsumer binder, int target, int componentLength, int toStoreCount) {
		this.binder = binder;
		this.target = target;
		this.componentLength = componentLength;
		this.store = StaticBuffers.allocateBuffer(toStoreCount * componentLength);
	}

	/**
	 * VAOs are not bound to global state, and we create a new one for each "shader object" anyways
	 */
	public static VertexBufferObjectBuilder vaoBound(int type, int componentLength) {
		return new VertexBufferObjectBuilder(i -> glBindBuffer(type, i), type, componentLength, 1024);
	}

	public void appendFrom(VertexBufferObjectBuilder builder, int off, int len) {
		this.copyFrom(this.totalCount(), builder, off, len, true);
	}

	public void copyFrom(int index, VertexBufferObjectBuilder src, int off, int objects, boolean updateSrc) {
		if(objects > 0) {
			if(src != this && updateSrc) {
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

	public void copyAttribute(int objectIndex, int byteOffset, int byteLen, VertexBufferObjectBuilder builder, int offset) {
		if(builder != this) {
			builder.upload(false);
		}
		this.resize((objectIndex + 1) * this.componentLength);
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

	public boolean upload(boolean forceBind) {
		boolean genned = false;
		if(this.storedCount > 0) {
			this.updateIfCopy();
			this.updateCopies();
			int newBufferLen = (this.objectIndex + this.storedCount) * this.componentLength;
			long bufferObjectLen = (long) this.objectIndex * this.componentLength;
			if(this.resize(newBufferLen)) {
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
			this.binder.accept(this.getId());
		}
		return genned;
	}

	public int getId() {
		VertexBufferObjectBuilder builder = this;
		while(builder.deferredSource != null) {
			builder = builder.deferredSource;
		}
		return builder.glId;
	}

	public int totalCount() {
		int i = this.objectIndex + this.storedCount;
		if(this.deferredSource != null) {
			i += this.copyCount;
		}
		return i;
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
		int neededSize = (this.storedCount+1) * this.componentLength;
		if(this.store.capacity() < neededSize) {
			ByteBuffer buffer = StaticBuffers.allocateBuffer(JMath.nearestPowerOf2(neededSize + 1024));
			buffer.put(0, this.store, 0, this.storedCount * this.componentLength);
			this.store = buffer;
		}
	}

	public void close() {
		if(this.glId != 0) {
			this.updateCopies();
			glDeleteBuffers(this.glId);
		}
	}

	public void updateIfCopy() {
		VertexBufferObjectBuilder source = this.deferredSource;
		if(source != null) {
			List<VertexBufferObjectBuilder> copies = source.copies;
			if(copies != null) {
				copies.remove(this);
			}
			this.deferredSource = null;
			this.copyFrom(0, source, 0, this.copyCount, false);
		}
	}

	public void updateCopies() {
		if(this.copies != null) { // copy to all BufferedObjectBuilders that needed this instance
			List<VertexBufferObjectBuilder> to = this.copies;
			this.copies = null;
			for(VertexBufferObjectBuilder copy : to) {
				copy.deferredSource = null;
				copy.copyFrom(0, this, 0, copy.copyCount, true);
			}
		}
	}

	private boolean resize(int newBufferLen) {
		if(newBufferLen > this.bufferLength || this.glId == 0) {
			int alloc = JMath.nearestPowerOf2(newBufferLen + 1024);
			int old = this.glId;
			int new_ = this.glId = glGenBuffers();
			this.binder.accept(new_);
			glBufferData(this.target, alloc, GL_STATIC_DRAW);
			int bufferObjectLen = this.objectIndex * this.componentLength;
			if(bufferObjectLen > 0) {
				glBindBuffer(GL_COPY_READ_BUFFER, old);
				glCopyBufferSubData(GL_COPY_READ_BUFFER, this.target, 0, 0, bufferObjectLen);
			}
			if(old != 0) {
				glDeleteBuffers(old);
			}
			this.bufferLength = alloc;
			return true;
		} else {
			return false;
		}
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.store;
	}
}
