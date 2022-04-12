package net.devtech.jerraria.network;

import io.netty.buffer.ByteBuf;

public interface Encodable {

	void encode(ByteBuf buf);
}
