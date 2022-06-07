package net.devtech.jerraria.render.internal.buffers;

import net.devtech.jerraria.render.api.basic.GlData;

public interface BufferObjectBuilderAccess extends GlData.Buf, GlData.ReadableBuf {
	BufferObjectBuilderAccess struct(int structIndex);

	BufferObjectBuilderAccess variable(int variableIndex);

	default BufferObjectBuilderAccess structVariable(int structIndex, int variableIndex) {
		return this.struct(structIndex).variable(variableIndex);
	}

	void copyFrom(BufferObjectBuilderAccess src, int from, int to, int fromOffset, int toOffset, int len);

	AbstractBOBuilder getRoot();

	void loadFeedback();
}
