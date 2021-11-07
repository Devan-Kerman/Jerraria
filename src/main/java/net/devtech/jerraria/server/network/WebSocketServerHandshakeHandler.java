package net.devtech.jerraria.server.network;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jetbrains.annotations.NotNull;

import java.net.URI;

public class WebSocketServerHandshakeHandler extends ChannelInboundHandlerAdapter {

	private final URI uri;
	private WebSocketServerHandshaker handshaker;

	public WebSocketServerHandshakeHandler(URI uri) {
		this.uri = uri;
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		if (msg instanceof FullHttpRequest request) {
			handle(ctx, request);
		} else if (msg instanceof CloseWebSocketFrame closeFrame) {
			handshaker.close(ctx.channel(), closeFrame);
		} else if (msg instanceof PingWebSocketFrame) {
			ctx.write(msg);
		} else {
			ctx.fireChannelRead(msg);
		}
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) {
		ctx.flush();
	}

	private void handle(ChannelHandlerContext ctx, FullHttpRequest request) {
		if (!request.decoderResult().isSuccess()) {
			ctx.write(HttpResponseStatus.BAD_REQUEST).addListener(ChannelFutureListener.CLOSE);
		} else {
			handshaker = new WebSocketServerHandshakerFactory(uri.toString(), null, true).newHandshaker(request);

			if (handshaker == null) {
				WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel()).addListener(ChannelFutureListener.CLOSE);
			} else {
				handshaker.handshake(ctx.channel(), request);
			}
		}
	}
}
