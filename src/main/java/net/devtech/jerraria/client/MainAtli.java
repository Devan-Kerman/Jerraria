package net.devtech.jerraria.client;

import java.util.concurrent.CompletableFuture;

import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.render.textures.Atlas;

public class MainAtli {
	public static LoadRender render;
	public static Atlas main;

	static {
		RENDER = LoadRender.create("Stitching main atlas", 1);
		MAIN = Atlas.createAtlas(RENDER, Id.create("jerraria", "main"));
	}
}
