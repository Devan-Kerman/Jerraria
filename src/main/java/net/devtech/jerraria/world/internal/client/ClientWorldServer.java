package net.devtech.jerraria.world.internal.client;

import java.util.ArrayList;
import java.util.List;

import it.unimi.dsi.fastutil.Arrays;
import net.devtech.jerraria.client.JerrariaClient;
import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;

public class ClientWorldServer implements WorldServer {
	public final List<World> worlds = new ArrayList<>();

	@Override
	public World getById(int sessionId) {
		return this.worlds.get(sessionId);
	}

	@Override
	public VirtualFile.Directory getResources() {
		return JerrariaClient.CLIENT_RESOURCES;
	}

	public void add(World world) {
		this.worlds.add(world);
	}
}
