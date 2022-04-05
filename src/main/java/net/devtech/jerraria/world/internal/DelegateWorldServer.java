package net.devtech.jerraria.world.internal;

import net.devtech.jerraria.resource.VirtualFile;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.WorldServer;

public class DelegateWorldServer implements WorldServer {
	final WorldServer delegate;

	public DelegateWorldServer(WorldServer delegate) {this.delegate = delegate;}

	@Override
	public World getById(int sessionId) {
		return delegate.getById(sessionId);
	}

	@Override
	public VirtualFile.Directory getResources() {
		return delegate.getResources();
	}
}
