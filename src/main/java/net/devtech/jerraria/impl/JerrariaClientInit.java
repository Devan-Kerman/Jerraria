package net.devtech.jerraria.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Vector;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class JerrariaClientInit implements ClientModInitializer {
	static ResourceManager manager;

	static String source(Id id, String ext) {
		Identifier path = new Identifier(id.mod(), id.path() + ext);
		ResourceManager manager = getManager();
		return manager.getResource(path).map(resource -> {
			try(BufferedReader reader = resource.getReader()) {
				return reader.lines().collect(Collectors.joining("\n"));
			} catch(IOException e) {
				e.printStackTrace();
				return null;
			}
		}).orElse(null);
	}

	@Override
	public void onInitializeClient() {
		ShaderManager.FRAG_SOURCES.add((current, path) -> Pair.of(current, source(path, ".frag"))); // fragment shaders
		ShaderManager.VERT_SOURCES.add((current, path) -> Pair.of(current, source(path, ".vert"))); // vertex shaders
		ShaderManager.LIB_SOURCES.add((current, path) -> Pair.of(current, source(path, ".glsl"))); // utility glsl code
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES).registerReloadListener(new SimpleSynchronousResourceReloadListener() {
			final Identifier id = new Identifier("jerraria", "shaders");

			@Override
			public void reload(ResourceManager manager) {
				JerrariaClientInit.manager = manager;
				for(Shader<?> shader : ShaderImpl.SHADERS) {
					shader.reload();
				}
				JerrariaClientInit.manager = null;
			}

			@Override
			public Identifier getFabricId() {
				return this.id;
			}
		});
	}

	public static ResourceManager getManager() {
		ResourceManager manager = JerrariaClientInit.manager;
		if(manager == null) {
			JerrariaClientInit.manager = manager = MinecraftClient.getInstance().getResourceManager();
		}
		return manager;
	}
}
