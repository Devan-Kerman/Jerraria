package net.devtech.jerraria.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

public class SplitterCodec extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		in.markReaderIndex();

		while (in.readableBytes() > Integer.BYTES) {
			int length = in.readInt();

			if (in.readableBytes() >= length) {
				in.resetReaderIndex();
				out.add(in.readBytes(Integer.BYTES + length));
				in.markReaderIndex();
			} else {
				break;
			}
		}
	}
}
