package net.devtech.jerraria.world.internal;

import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;

public class JerrariaWorldServer implements WorldServer {
	final VirtualFile.Directory resources;


	public JerrariaWorldServer(VirtualFile.Directory resources) {
		this.resources = resources;
	}

	@Override
	public World getById(int sessionId) {
		return null;
	}

	@Override
	public VirtualFile.Directory getServerResources() {
		return null;
	}
}
