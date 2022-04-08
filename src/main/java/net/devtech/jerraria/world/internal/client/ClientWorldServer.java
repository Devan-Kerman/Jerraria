package net.devtech.jerraria.world.internal.client;

import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;

public class ClientWorldServer implements WorldServer {
	public final World[] worlds;

	public ClientWorldServer(World[] worlds) {
		this.worlds = worlds;
	}

	@Override
	public World getById(int sessionId) {
		return this.worlds[sessionId];
	}

	@Override
	public VirtualFile.Directory getResources() {
		return JerrariaClient.CLIENT_RESOURCES;
	}
}
