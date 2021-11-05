package net.devtech.jerraria.server.network;

import io.netty.channel.epoll.Epoll;
import io.netty.channel.kqueue.KQueue;
import org.jetbrains.annotations.Nullable;

public interface Nettyworking {

	static <T> T select(T nio, @Nullable T epoll, @Nullable T kqueue) {
		if (epoll != null && Epoll.isAvailable()) {
			return epoll;
		} else if (kqueue != null && KQueue.isAvailable()) {
			return kqueue;
		} else {
			return nio;
		}
	}
}
