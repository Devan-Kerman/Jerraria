package net.devtech.jerraria.network.network;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.util.concurrent.PromiseCombiner;
import org.jetbrains.annotations.NotNull;

public class Pagination extends ChannelDuplexHandler {

	private static final int PAGINATED_PACKET_ID = -1;
	private static final int MAX_PACKET_SIZE = 8192;

	private ByteBuf accumulator;

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) {
		ctx.fireChannelActive();

		accumulator = ctx.alloc().buffer();
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		if (msg instanceof PacketCodec.Packet packet) {
			if (packet.channel() == PAGINATED_PACKET_ID) {
				accumulator.writeBytes(packet.data());
				return;
			} else if (accumulator.isReadable()) {
				if (packet.data() instanceof EmptyByteBuf) {
					msg = packet = new PacketCodec.Packet(packet.channel(), ctx.alloc().buffer());
				}

				packet.data().writeBytes(accumulator);
				accumulator.clear();
			}
		}

		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
		PromiseCombiner combiner = new PromiseCombiner(ctx.executor());

		if (msg instanceof PacketCodec.Packet packet) {
			ByteBuf data = packet.data();

			while (data.isReadable(MAX_PACKET_SIZE)) {
				combiner.add(ctx.write(new PacketCodec.Packet(PAGINATED_PACKET_ID, data.readBytes(MAX_PACKET_SIZE))));
			}
		}

		combiner.add(ctx.write(msg));
		combiner.finish(promise);
	}
}
