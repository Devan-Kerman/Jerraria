package net.devtech.jerraria.server.network;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketHandshakeException;
import org.jetbrains.annotations.NotNull;

public class WebSocketClientHandshakeHandler extends ChannelInboundHandlerAdapter {

	private final WebSocketClientHandshaker handshaker;
	private ChannelPromise promise;

	public WebSocketClientHandshakeHandler(WebSocketClientHandshaker handshaker) {
		this.handshaker = handshaker;
	}

	public ChannelPromise getPromise() {
		return promise;
	}

	@Override
	public void handlerAdded(ChannelHandlerContext ctx) {
		promise = ctx.newPromise();
	}

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) {
		handshaker.handshake(ctx.channel());
		ctx.fireChannelActive();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.writeAndFlush(new CloseWebSocketFrame()).sync();
		ctx.fireChannelInactive();
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		Channel channel = ctx.channel();

		if (!handshaker.isHandshakeComplete()) {
			try {
				handshaker.finishHandshake(channel, (FullHttpResponse) msg);
				promise.setSuccess();
			} catch (WebSocketHandshakeException exception) {
				promise.setFailure(exception);
			}
		} else if (msg instanceof CloseWebSocketFrame) {
			handshaker.close(channel, ((CloseWebSocketFrame) msg).retain());
		} else {
			ctx.fireChannelRead(msg);
		}
	}
}
