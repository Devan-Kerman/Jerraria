package net.devtech.jerraria.server.dedicated;

import net.devtech.jerraria.server.JServer;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;

public class DedicatedServer extends JServer {

	private boolean stopped;

	public static void main(String[] args) throws URISyntaxException {
		// todo: read from config
		ServerConfig config = new ServerConfig(new ServerConfig.NetworkConfig(new URI("ws://localhost:8008/"), "::1", 8008));

		DedicatedServer server = new DedicatedServer();
		server.open(config.network().uri(), new InetSocketAddress(config.network().bind(), config.network().port())).syncUninterruptibly();

		// todo: wait for shutdown signal
		while (!server.stopped) {
			// todo: server tick
			Thread.onSpinWait();
		}

		server.closeConnections();
	}
}
