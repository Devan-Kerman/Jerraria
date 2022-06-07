package net.devtech.jerraria.render.internal.buffers;

import net.devtech.jerraria.render.api.basic.GlData;

public interface BufferObjectBuilderAccess extends GlData.Buf {
	BufferObjectBuilderAccess struct(int structIndex);

	BufferObjectBuilderAccess variable(int variableIndex);

	BufferObjectBuilderAccess structVariable(int structIndex, int variableIndex);

	void copyFrom(BufferObjectBuilderAccess src, int from, int to, int fromOffset, int toOffset, int len);

	AbstractBOBuilder getRoot();
}
