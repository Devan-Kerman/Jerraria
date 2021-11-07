package net.devtech.jerraria.client.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.EmptyHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.devtech.jerraria.server.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class TestClient {

	public static void main(String[] args) throws InterruptedException {
		byte[] data = new byte[65536];
		ThreadLocalRandom.current().nextBytes(data);

		Supplier<EventLoopGroup> supplier = Nettyworking.select(NioEventLoopGroup::new, EpollEventLoopGroup::new, KQueueEventLoopGroup::new);
		EventLoopGroup group = supplier.get();

		WebSocketClientHandshakeHandler handler = new WebSocketClientHandshakeHandler(WebSocketClientHandshakerFactory.newHandshaker(URI.create("ws://localhost:8008/help_me"), WebSocketVersion.V13, null, true, EmptyHttpHeaders.INSTANCE));

		try {
			Channel channel = new Bootstrap()
				.group(group)
				.channel(Nettyworking.select(NioSocketChannel.class, EpollSocketChannel.class, KQueueSocketChannel.class))
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(@NotNull SocketChannel channel) {
						channel.pipeline()
							.addLast("timeout", new ReadTimeoutHandler(30))
							// only when wss scheme is used
							// use null instead of insecure when we want encryption + verification
							// .addLast(SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build().newHandler(channel.alloc(), group))
							.addLast("http", new HttpClientCodec())
							.addLast("http_aggregator", new HttpObjectAggregator(8192))
							.addLast("compression", WebSocketClientCompressionHandler.INSTANCE)
							.addLast("handshake", handler)
							.addLast("heartbeat", new KeepAlive())
							.addLast("websocket_codec", new WebSocketFrameCodec())
							.addLast("codec", new PacketCodec())
							.addLast("splitter", new Pagination())
							.addLast("connection", new ChannelInboundHandlerAdapter() {
								@Override
								public void channelActive(@NotNull ChannelHandlerContext ctx) {
									ctx.fireChannelActive();
									ctx.writeAndFlush(new PacketCodec.Packet(1, ctx.alloc().buffer().writeBytes(data)));
								}

								@Override
								public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
									byte[] received = new byte[data.length];
									((PacketCodec.Packet) msg).data().readBytes(received, 0, data.length);

									if (Arrays.equals(data, received)) {
										System.out.println("Data sent and received successfully");
									} else {
										System.err.println("Data mismatch through echo server");
									}

									ctx.close();
								}
							});
					}
				})
				.connect("::1", 8008)
				.sync()
				.channel();
			channel.closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
}
