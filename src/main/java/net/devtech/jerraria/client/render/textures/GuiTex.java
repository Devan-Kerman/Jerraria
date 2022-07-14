package net.devtech.jerraria.client.render.textures;

import net.devtech.jerraria.gui.api.icons.borders.NinePatch;
import net.devtech.jerraria.gui.api.icons.borders.NinePatchBorder;
import net.devtech.jerraria.render.api.textures.Texture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;

public class GuiTex {
	public static final Identifier BUTTON_ATLAS_ID = new Identifier("jerraria", "button");
	public static final Texture BUTTON_ATLAS = new Texture(() -> {
		TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		AbstractTexture abstractTexture = textureManager.getTexture(BUTTON_ATLAS_ID);
		return abstractTexture.getGlId();
	}, 0, 0, 1, 1);
	public static final Texture DISABLED = BUTTON_ATLAS.section(0, 0, 1, 80f/512f);
	public static final Texture ENABLED = BUTTON_ATLAS.section(0, 80f/512f, 1, 80f/512f);
	public static final Texture HIGHLIGHTED_BLUE = BUTTON_ATLAS.section(0, 160f/512f, 1, 80f/512f);
	public static final Texture HIGHLIGHTED_DARK = BUTTON_ATLAS.section(0, 240f/512f, 1, 80f/512f);
	public static final Texture HIGHLIGHTED_LIGHT = BUTTON_ATLAS.section(0, 320f/512f, 1, 80f/512f);

	public static final NinePatch<Integer> PATCH_DISABLED = NinePatchBorder.patch(DISABLED).cornerSize(2).cornerUv(4/256f, 4/80f).build();
	public static final NinePatch<Integer> PATCH_ENABLED = NinePatchBorder.patch(ENABLED).cornerSize(2).cornerUv(4/256f, 4/80f).build();
	public static final NinePatch<Integer> PATCH_HIGHLIGHTED_BLUE = NinePatchBorder.patch(HIGHLIGHTED_BLUE).cornerSize(2).cornerUv(4/256f, 4/80f).build();
	public static final NinePatch<Integer> PATCH_HIGHLIGHTED_DARK = NinePatchBorder.patch(HIGHLIGHTED_DARK).cornerSize(2).cornerUv(4/256f, 4/80f).build();
	public static final NinePatch<Integer> PATCH_HIGHLIGHTED_LIGHT = NinePatchBorder.patch(HIGHLIGHTED_LIGHT).cornerSize(2).cornerUv(4/256f, 4/80f).build();
}
