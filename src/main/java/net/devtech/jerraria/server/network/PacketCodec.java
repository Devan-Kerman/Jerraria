package net.devtech.jerraria.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class PacketCodec extends ByteToMessageCodec<PacketCodec.Packet> {

	public record Packet(int channel, ByteBuf data) {
	}

	@Override
	protected void encode(ChannelHandlerContext ctx, Packet msg, ByteBuf out) {
		// Send channel and data length
		out.writeInt(Integer.BYTES + msg.data().readableBytes());
		out.writeInt(msg.channel());
		out.writeBytes(msg.data());
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
		in.markReaderIndex();

		while (in.isReadable(Integer.BYTES)) {
			int length = in.readInt();

			if (length == 0) {
				// skip
				in.markReaderIndex();
			} else if (length >= Integer.BYTES && in.isReadable(length)) {
				int channel = in.readInt();
				ByteBuf data = in.readBytes(length - Integer.BYTES);
				out.add(new Packet(channel, data));
				in.markReaderIndex();
			} else {
				break;
			}
		}

		in.resetReaderIndex();
	}
}
