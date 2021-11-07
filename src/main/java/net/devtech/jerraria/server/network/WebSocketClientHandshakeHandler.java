package net.devtech.jerraria.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jetbrains.annotations.NotNull;

public class WebSocketClientHandshakeHandler extends ChannelInboundHandlerAdapter {

	private final WebSocketClientHandshaker handshaker;

	public WebSocketClientHandshakeHandler(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		Channel channel = ctx.channel();

		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(channel, (FullHttpResponse) msg);
			ctx.fireChannelActive();
		} else if (msg instanceof CloseWebSocketFrame closeFrame) {
			handshaker.close(channel, closeFrame);
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}
