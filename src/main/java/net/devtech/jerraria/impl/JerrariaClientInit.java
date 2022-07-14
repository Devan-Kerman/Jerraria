package net.devtech.jerraria.impl;

import java.util.Vector;

import net.devtech.jerraria.render.api.Shader;

import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class JerrariaClientInit implements ClientModInitializer {
	static ResourceManager manager;
	public static final Vector<Shader<?>> TO_RELOAD = new Vector<>();

	@Override
	public void onInitializeClient() {
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			final Identifier id = new Identifier("jerraria", "shaders");

			@Override
			public void reload(ResourceManager manager) {
				JerrariaClientInit.manager = manager;

			}

			@Override
			public Identifier getFabricId() {
				return this.id;
			}
		});
	}

	public static ResourceManager getManager() {

	}
}
