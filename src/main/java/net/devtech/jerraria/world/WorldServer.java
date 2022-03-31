package net.devtech.jerraria.world;

import net.devtech.jerraria.resource.VirtualFile;

public interface WorldServer {
	World getById(int sessionId);

	VirtualFile.Directory getServerResources();
}
