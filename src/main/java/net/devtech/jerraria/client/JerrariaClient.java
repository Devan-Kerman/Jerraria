package net.devtech.jerraria.client;

import net.devtech.jerraria.client.render.textures.Atlas;
import net.devtech.jerraria.resource.VirtualFile;

public class JerrariaClient {
	public static final long MAIN_WINDOW_GL_ID = ClientInit.glMainWindow;
	public static final Atlas MAIN_ATLAS = ClientInit.mainAtlas;
	public static final VirtualFile.Directory CLIENT_RESOURCES = Bootstrap.clientResources;

	public static int windowWidth() {
		return ClientInit.dims[0];
	}

	public static int windowHeight() {
		return ClientInit.dims[1];
	}

	static {
		if(MAIN_ATLAS == null) {
			throw new IllegalStateException(JerrariaClient.class.getSimpleName() + " loaded by " + ClientInit.class.getSimpleName());
		}
	}

}
