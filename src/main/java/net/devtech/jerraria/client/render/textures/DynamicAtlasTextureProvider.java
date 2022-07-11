package net.devtech.jerraria.client.render.textures;

import java.util.function.Function;

import net.devtech.jerraria.util.Id;

public interface DynamicAtlasTextureProvider {
	void provideTextures(Id atlasId, Function<DynamicAtlasTexture, RedrawCallback> function);
}
