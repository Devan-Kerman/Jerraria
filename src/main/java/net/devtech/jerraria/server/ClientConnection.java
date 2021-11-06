package net.devtech.jerraria.server;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import net.devtech.jerraria.server.network.NetworkSide;
import org.jetbrains.annotations.NotNull;

public class ClientConnection extends ChannelDuplexHandler {

	private final NetworkSide side;

	public ClientConnection(NetworkSide side) {
		this.side = side;
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		// Echo server
		ctx.writeAndFlush(msg);
	}
}
