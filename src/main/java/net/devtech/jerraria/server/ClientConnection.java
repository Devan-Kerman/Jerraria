package net.devtech.jerraria.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;
import net.devtech.jerraria.server.network.NetworkSide;
import org.jetbrains.annotations.NotNull;

public class ClientConnection extends ChannelDuplexHandler {

	private final NetworkSide side;

	public ClientConnection(NetworkSide side) {
		this.side = side;
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		if (msg instanceof ByteBuf buf) {
			// todo: read channel
			// todo: defer to whoever wants to handle it

			ctx.writeAndFlush(buf);
		} else {
			ReferenceCountUtil.release(msg);
		}
	}
}
