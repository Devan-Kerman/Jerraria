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
import io.netty.handler.timeout.ReadTimeoutHandler;
import net.devtech.jerraria.server.network.KeepAlive;
import net.devtech.jerraria.server.network.Nettyworking;
import net.devtech.jerraria.server.network.PacketCodec;
import net.devtech.jerraria.server.network.Pagination;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Supplier;

public class TestClient {

	public static void main(String[] args) throws InterruptedException {
		byte[] data = new byte[65536];
		ThreadLocalRandom.current().nextBytes(data);

		Supplier<EventLoopGroup> supplier = Nettyworking.select(NioEventLoopGroup::new, EpollEventLoopGroup::new, KQueueEventLoopGroup::new);
		EventLoopGroup group = supplier.get();

		try {
			new Bootstrap()
				.group(group)
				.channel(Nettyworking.select(NioSocketChannel.class, EpollSocketChannel.class, KQueueSocketChannel.class))
				.option(ChannelOption.SO_KEEPALIVE, true)
				.option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					protected void initChannel(@NotNull SocketChannel channel) {
						channel.pipeline()
							.addLast("timeout", new ReadTimeoutHandler(30))
							.addLast("codec", new PacketCodec())
							.addLast("splitter", new Pagination())
							.addLast("heartbeat", new KeepAlive())
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

									if (!Arrays.equals(data, received)) {
										System.err.println("Data mismatch through echo server");
									} else {
										System.out.println("Data sent and received successfully");
									}
								}
							});
					}
				})
				.connect("::1", 8008)
				.sync()
				.channel().closeFuture().sync();
		} finally {
			group.shutdownGracefully();
		}
	}
}
