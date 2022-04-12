package net.devtech.jerraria.network;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.devtech.jerraria.network.network.NetworkSide;
import net.devtech.jerraria.network.network.PacketCodec;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class ClientConnection extends ChannelDuplexHandler {

	private final NetworkSide side;
	private final Deque<Function<ByteBufAllocator, PacketCodec.Packet>> sendQueue = new LinkedList<>();
	private final Multimap<Integer, Consumer<ByteBuf>> handlers = Multimaps.newListMultimap(new Int2ObjectOpenHashMap<>(), ArrayList::new);

	public ClientConnection(NetworkSide side) {
		this.side = side;
	}

	@Override
	public void channelActive(@NotNull ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);

		ctx.executor().scheduleAtFixedRate(() -> {
			Function<ByteBufAllocator, PacketCodec.Packet> packetFactory;

			while ((packetFactory = this.sendQueue.poll()) != null) {
				ctx.write(packetFactory.apply(ctx.alloc()));
			}

			ctx.flush();
		}, 0, 10, TimeUnit.MILLISECONDS);

		{
			// best test case
			record Data(int i, short s) implements Encodable {
				@Override
				public void encode(ByteBuf buf) {
					buf.writeInt(this.i);
					buf.writeShort(this.s);
				}
			}

			this.registerHandler(1, data -> new Data(data.readInt(), data.readShort()), System.out::println);

			this.queuePacket(1, new Data(1, (short) 0));
		}
	}

	@Override
	public void channelRead(@NotNull ChannelHandlerContext ctx, @NotNull Object msg) {
		if (msg instanceof PacketCodec.Packet packet) {
			var handlers = this.handlers.get(packet.channel());

			if (handlers.isEmpty()) {
				// TODO: Received unknown packet
			}

			for (Consumer<ByteBuf> handler : handlers) {
				handler.accept(packet.data().copy());
			}
		}
	}

	/**
	 * Queues a packet to be sent
	 *
	 * @param channel Channel
	 * @param data    Packet to send
	 */
	public void queuePacket(int channel, ByteBuf data) {
		this.sendQueue.add($ -> new PacketCodec.Packet(channel, data));
	}

	/**
	 * Queues a packet to be sent
	 *
	 * @param channel Channel
	 * @param data    Packet to send
	 */
	public void queuePacket(int channel, Encodable data) {
		this.sendQueue.add(alloc -> {
			var buffer = alloc.buffer();
			data.encode(buffer);
			return new PacketCodec.Packet(channel, buffer);
		});
	}

	/**
	 * Register a handler for incoming well-formed packets
	 *
	 * @param channel Channel to listen on
	 * @param handler Function which handles the packet, runs on the main thread
	 */
	public void registerHandler(int channel, Runnable handler) {
		if (!this.handlers.put(channel, $ -> {
			// TODO: Run handler back in the main thread
			handler.run();
		})) {
			// TODO: Warn about duplicate handler
		}
	}

	/**
	 * Register a handler for incoming well-formed packets
	 *
	 * @param channel Channel to listen on
	 * @param reader  Function to convert the packet body to intermediate data. No guarantee is made on which thread this is run on
	 * @param handler Function which handles the data, runs on the main thread
	 * @param <T>     Intermediate data type
	 */
	public <T> void registerHandler(int channel, Function<ByteBuf, T> reader, Consumer<T> handler) {
		if (!this.handlers.put(channel, buf -> {
			// TODO: Run handler back in the main thread
			handler.accept(reader.apply(buf));
		})) {
			// TODO: Warn about duplicate handler
		}
	}
}
