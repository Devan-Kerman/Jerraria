package net.devtech.jerraria.network.dedicated;

import java.net.URI;

public record ServerConfig(NetworkConfig network) {

	public record NetworkConfig(URI uri, String bind, int port) {
	}
}
