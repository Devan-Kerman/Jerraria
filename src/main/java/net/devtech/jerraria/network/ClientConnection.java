package net.devtech.jerraria.network;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.devtech.jerraria.network.network.NetworkSide;
import org.jetbrains.annotations.NotNull;

public class ClientConnection extends ChannelInboundHandlerAdapter {

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