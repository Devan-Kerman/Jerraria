package net.devtech.jerraria.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.kqueue.KQueueEventLoopGroup;
import io.netty.channel.kqueue.KQueueServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.devtech.jerraria.server.network.*;
import org.jetbrains.annotations.NotNull;

import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;

public abstract class JServer {

	private final List<Listener> listeners = new ArrayList<>();

	public ChannelFuture open(URI uri, SocketAddress address) {
		Supplier<EventLoopGroup> supplier = Nettyworking.select(NioEventLoopGroup::new, EpollEventLoopGroup::new, KQueueEventLoopGroup::new);
		EventLoopGroup parent = supplier.get();
		EventLoopGroup child = supplier.get();

		// client side
		// player.x += x;
		// player.y += y;
		//
		// player.queuePacketWithResponse(MOVEMENT, Buffer.create().write(x).write(y)).await(response -> {
		//     player.x -= x;
		//     player.y -= y;
		// });

		// server side
		// player.listen(MOVEMENT, (buf, response) -> {
		//     int x = buffer.readInt();
		//     int y = buffer.readInt();
		//
		//     server.execute(() -> {
		//         if (!applyMovement(x, y)) {
		//             response.queue(Buffer.empty());
		//         }
		//     });
		// });

		ChannelFuture future = new ServerBootstrap()
			.group(parent, child)
			.channel(Nettyworking.select(NioServerSocketChannel.class, EpollServerSocketChannel.class, KQueueServerSocketChannel.class))
			.childHandler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(@NotNull SocketChannel channel) {
					channel.pipeline()
						.addLast("timeout", new ReadTimeoutHandler(30))
						.addLast("http", new HttpServerCodec())
						.addLast("http_aggregator", new HttpObjectAggregator(8192))
						// this should be toggleable in case a reverse proxy wants to compress instead
						.addLast("compression", new WebSocketServerCompressionHandler())
						.addLast("handshake", new WebSocketServerHandshakeHandler(uri))
						.addLast("websocket_codec", new WebSocketFrameCodec())
						.addLast("codec", new PacketCodec())
						.addLast("splitter", new Pagination())
						.addLast("heartbeat", new KeepAlive())
						.addLast("connection", createClientConnection());
				}
			})
			.childOption(ChannelOption.TCP_NODELAY, true)
			.localAddress(address)
			.bind();

		listeners.add(new Listener(parent, child, future.channel()));
		return future;
	}

	public ClientConnection createClientConnection() {
		return new ClientConnection(NetworkSide.SERVER_BOUND);
	}

	public void closeConnections() {
		Iterator<Listener> iterator = listeners.iterator();

		while (iterator.hasNext()) {
			Listener listener = iterator.next();
			listener.channel().closeFuture().syncUninterruptibly();
			listener.parent().shutdownGracefully();
			listener.child().shutdownGracefully();

			iterator.remove();
		}
	}

	private record Listener(EventLoopGroup parent, EventLoopGroup child, Channel channel) {
	}
}
