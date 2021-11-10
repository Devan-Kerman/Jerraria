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
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpClientCodec;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import io.netty.handler.codec.http.websocketx.WebSocketClientProtocolHandler;
import io.netty.handler.codec.http.websocketx.WebSocketVersion;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketClientCompressionHandler;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.devtech.jerraria.server.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.URI;
import java.net.URISyntaxException;
import java.security.cert.Certificate;
import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class TestClient {

	public static void main(String[] args) throws InterruptedException, URISyntaxException {
		URI server = new URI("ws://localhost:8008/websocket");

		byte[] data = new byte[65536];
		ThreadLocalRandom.current().nextBytes(data);

		Supplier<EventLoopGroup> supplier = Nettyworking.select(NioEventLoopGroup::new, EpollEventLoopGroup::new, KQueueEventLoopGroup::new);
		EventLoopGroup group = supplier.get();

		// TLS encryption
		boolean isEncrypted = "wss".equalsIgnoreCase(server.getScheme());

		// Certificate validation to verify server's identity
		boolean isSecure = false;

		try {
			Channel channel = new Bootstrap()
				.group(group)
				.channel(Nettyworking.select(NioSocketChannel.class, EpollSocketChannel.class, KQueueSocketChannel.class))
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(@NotNull SocketChannel channel) throws Exception {
						channel.pipeline().addLast("timeout", new ReadTimeoutHandler(30));

						if (isEncrypted) {
							channel.pipeline().addLast("ssl", SslContextBuilder.forClient()
								.trustManager(isSecure ? null : InsecureTrustManagerFactory.INSTANCE)
								.build()
								.newHandler(channel.alloc(), server.getHost(), server.getPort(), channel.eventLoop()));
						}

						channel.pipeline()
							.addLast("http", new HttpClientCodec())
							.addLast("http_aggregator", new HttpObjectAggregator(65536))
							.addLast("compression", WebSocketClientCompressionHandler.INSTANCE)
							.addLast("websocket_protocol", new WebSocketClientProtocolHandler(WebSocketClientHandshakerFactory.newHandshaker(server, WebSocketVersion.V13, null, true, new DefaultHttpHeaders())))
							.addLast("heartbeat", new KeepAlive())
							.addLast("websocket_codec", new WebSocketFrameCodec())
							.addLast("codec", new PacketCodec())
							.addLast("splitter", new Pagination())
							.addLast("connection", new ChannelInboundHandlerAdapter() {
								@Override
								public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
									if (evt == WebSocketClientProtocolHandler.ClientHandshakeStateEvent.HANDSHAKE_COMPLETE) {
										this.active(ctx);
									}

									ctx.fireUserEventTriggered(evt);
								}

								public void active(@NotNull ChannelHandlerContext ctx) throws Exception {
									if (ctx.pipeline().get("ssl") instanceof SslHandler ssl) {
										Certificate[] certificates = ssl.engine().getSession().getPeerCertificates();
										Certificate certificate = certificates[certificates.length - 1];

										System.out.println("Their certificate: " + certificate);
									}

									ctx.fireChannelActive();
									ctx.writeAndFlush(new PacketCodec.Packet(1, ctx.alloc().buffer().writeBytes(data)));
								}

								@Override
								public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
									if (msg instanceof PacketCodec.Packet packet) {
										byte[] received = new byte[data.length];
										packet.data().readBytes(received, 0, data.length);

										if (Arrays.equals(data, received)) {
											System.out.println("Data sent and received successfully");
										} else {
											System.err.println("Data mismatch through echo server");
										}

										ctx.close();
									}
								}
							});
					}
				})
				.connect(server.getHost(), server.getPort())
				.sync()
				.channel();
			channel.closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
}
