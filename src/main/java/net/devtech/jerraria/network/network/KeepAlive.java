package net.devtech.jerraria.network.network;

import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class KeepAlive extends ChannelDuplexHandler {

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) {
		ctx.fireChannelActive();

		ctx.executor().scheduleAtFixedRate(() -> {
			ctx.writeAndFlush(new PingWebSocketFrame());
		}, 0, 10, TimeUnit.SECONDS);
	}
}
