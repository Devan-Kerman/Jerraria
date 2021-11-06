package net.devtech.jerraria.server.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class KeepAlive extends ChannelDuplexHandler {

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) {
		ctx.fireChannelActive();

		ctx.executor().scheduleAtFixedRate(() -> {
			ctx.write(null);
		}, 0, 10, TimeUnit.SECONDS);
	}
}
