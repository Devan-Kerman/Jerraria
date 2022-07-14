package net.devtech.jerraria.client.render.textures;

/**
 * todo support in minecraft atlases
 * @param isDynamic whether the drawer should be redrawn every frame
 */
public record DynamicAtlasTexture(DynamicTextureDrawer drawer, String id, int width, int height, boolean isDynamic) {
	public DynamicAtlasTexture(DynamicTextureDrawer drawer, String id, int width, int height) {
		this(drawer, id, width, height, false);
	}

	public boolean isStatic() {
		return !this.isDynamic();
	}
}
