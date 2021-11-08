package net.devtech.jerraria.server.dedicated;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class ServerMain {

	public static void main(String[] args) throws URISyntaxException {
		// todo: read from config
		ServerConfig config = new ServerConfig(new ServerConfig.NetworkConfig(new URI("ws://localhost:8008/help_me"), "localhost", 8008));

		DedicatedServer server = new DedicatedServer();
		server.open(config.network().uri(), new InetSocketAddress(config.network().bind(), config.network().port())).syncUninterruptibly();

		// todo: wait for shutdown signal
		while (!server.isStopped()) {
			// todo: server tick
			Thread.onSpinWait();
		}

		server.closeConnections();
	}
}
