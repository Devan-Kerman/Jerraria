package net.devtech.jerraria.server.network;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jetbrains.annotations.NotNull;

public class WebSocketFrameCodec extends ChannelDuplexHandler {

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		if (msg instanceof BinaryWebSocketFrame binaryFrame) {
			msg = binaryFrame.content();
		}

		ctx.fireChannelRead(msg);
	}

	@Override
	public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
		if (msg instanceof ByteBuf buf) {
			ctx.write(new BinaryWebSocketFrame(buf).retain(), promise);
		} else {
			ctx.write(msg, promise);
		}
	}
}
