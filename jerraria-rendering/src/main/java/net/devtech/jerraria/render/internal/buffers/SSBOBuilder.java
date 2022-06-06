package net.devtech.jerraria.render.internal.buffers;

import static org.lwjgl.opengl.GL11.glGetInteger;
import static org.lwjgl.opengl.GL46.GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT;

import java.nio.ByteBuffer;
import java.util.BitSet;

import net.devtech.jerraria.render.internal.state.GLContextState;

// todo reunify the buffer builders
//  for VBOs, ignore fixed data, and ignore fractional uploading
public class SSBOBuilder extends AbstractUBOBuilder {
	public static final int SSBO_PADDING = glGetInteger(GL_SHADER_STORAGE_BUFFER_OFFSET_ALIGNMENT);
	final ByteBuffer fixedData;
	final BitSet fixedInitialized;
	public final int[] fixedIntervals;
	final int fixedDataLength;

	public SSBOBuilder(int fixedLen, int[] fixedElementOffsets, int structLen, int[] structVariableOffsets, int structsStart) {
		super(structLen, structLen, structVariableOffsets, structsStart);
		this.fixedData = BufferUtil.allocateBuffer(fixedLen);
		this.fixedInitialized = new BitSet(fixedElementOffsets.length);
		this.fixedIntervals = UBOBuilder.add(fixedElementOffsets, fixedLen);
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
		this.fixedData = BufferUtil.allocateBuffer(fixedLen);
	}

	@Override
	protected int getOffset(int structIndex) {
		if(structIndex == -2) {
			return 0;
		} else if(structIndex == -1) {
			return this.structsStart;
		}
		return super.getOffset(structIndex);
	}

	@Override
	protected int getOffset(int structIndex, int variableIndex) {
		if(structIndex == -2) {
			return this.fixedIntervals[variableIndex];
		}
		return super.getOffset(structIndex, variableIndex);
	}

	public void bind(int index) {
		this.flush();
		GLContextState.SHADER_BUFFER.bindBufferRange(index, this.glId, 0, this.bufferObjectLen);
	}

	@Override
	protected int padding() {
		return SSBO_PADDING;
	}

	@Override
	protected GLContextState.IndexedBufferTargetState state() {
		return GLContextState.SHADER_BUFFER;
	}
}
