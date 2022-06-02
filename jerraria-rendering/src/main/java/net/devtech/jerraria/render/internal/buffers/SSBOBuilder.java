package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT;
import static org.lwjgl.opengl.GL33.glBufferSubData;

import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.BitSet;

import net.devtech.jerraria.render.internal.state.GLContextState;

// todo reunify the buffer builders
//  for VBOs, ignore fixed data, and ignore fractional uploading
//  for EBOs, idk, maybe just uncringe the BufferBuilder system for it specifically
public class SSBOBuilder extends UBOBuilder {
	public static final int SSBO_PADDING = glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
	final ByteBuffer fixedData;
	final BitSet fixedInitialized;
	final int[] fixedIntervals;
	final int fixedDataLength;

	public SSBOBuilder(int fixedLen, int[] fixedElementOffsets, int structLen, int[] structVariableOffsets, int structsStart) {
		super(structLen, structLen, structVariableOffsets, structsStart);
		this.fixedData = ElementBufferBuilder.allocateBuffer(fixedLen);
		this.fixedInitialized = new BitSet(fixedElementOffsets.length);
		this.fixedIntervals = add(fixedElementOffsets, fixedLen);
		this.fixedDataLength = fixedLen;
	}

	public SSBOBuilder(SSBOBuilder buffer) {
		super(buffer);
		this.fixedIntervals = buffer.fixedIntervals;
		int fixedLen = buffer.fixedDataLength;
		this.fixedDataLength = fixedLen;
		this.fixedInitialized = new BitSet(buffer.fixedInitialized.length());
		this.glId = buffer.glId;
		this.bufferObjectLen = buffer.bufferObjectLen;
		this.structIndex = buffer.structIndex;
		this.fixedData = ElementBufferBuilder.allocateBuffer(fixedLen);
	}

	public static int[] add(int[] arr, int val) {
		int last = arr.length, copy[] = Arrays.copyOf(arr, last + 1);
		copy[last] = val;
		return copy;
	}

	public void offset(int fixedOffset) {
		this.fixedInitialized.set(fixedOffset);
		this.primary = this.fixedData.position(this.fixedIntervals[fixedOffset]);
	}

	@Override
	protected void flush() {
		super.flush();
		this.uploadFixed();
	}

	public void bind(int index) {
		this.flush();
		if(this.bufferObjectLen != 0) {
			GLContextState.SHADER_BUFFER.bindBufferRange(index, this.glId, 0, this.bufferObjectLen);
		}
	}

	private void uploadFixed() {
		if(!this.fixedInitialized.isEmpty()) {
			this.evaluateDeferredCopies();
			this.ensureBufferObjectCapacity(this.fixedDataLength);
			GLContextState.SHADER_BUFFER.bindBuffer(this.glId);
			uploadIntervals(GLContextState.SHADER_BUFFER.type,
				this.fixedInitialized,
				this.fixedData,
				this.fixedIntervals,
				0
			);
		}
	}

	@Override
	protected GLContextState.IndexedBufferTargetState targetState() {
		return GLContextState.SHADER_BUFFER;
	}

	@Override
	protected ByteBuffer getBuffer() {
		return this.primary;
	}

	@Override
	protected int padding() {
		return SSBO_PADDING;
	}
}
