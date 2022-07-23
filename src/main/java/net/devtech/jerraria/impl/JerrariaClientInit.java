package net.devtech.jerraria.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Pair;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.render.api.ShaderImpl;
import net.devtech.jerraria.render.api.impl.RenderingEnvironmentInternal;
import net.devtech.jerraria.render.internal.ShaderManager;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.util.Validate;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.fabric.api.resource.SimpleSynchronousResourceReloadListener;

public class JerrariaClientInit implements ClientModInitializer {
	static ResourceManager manager;

	static String source(Id id, String ext) {
		ResourceManager manager = getManager();
		Optional<String> reader = manager.getResource(new Identifier(id.mod(), "shaders/"+id.path() + ext)).map(r -> {
			try {
				return r.getReader();
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		}).map(reader1 ->  {
			try(reader1) {
				return reader1.lines().collect(Collectors.joining("\n"));
			} catch(IOException e) {
				throw Validate.rethrow(e);
			}
		});
		return reader.orElse(null);
	}

	@Override
	public void onInitializeClient() {
		RenderingEnvironmentInternal.renderThread_ = Thread.currentThread();
		ShaderManager.FRAG_SOURCES.add((current, path) -> Pair.of(current, source(path, ".frag"))); // fragment shaders
		ShaderManager.VERT_SOURCES.add((current, path) -> Pair.of(current, source(path, ".vert"))); // vertex shaders
		ShaderManager.LIB_SOURCES.add((current, path) -> Pair.of(current, source(path, ".glsl"))); // utility glsl code
		ShaderManager.SHADER_PROVIDERS.add(id -> new ShaderManager.ShaderPair(id, id));
		ShaderManager.SHADER_PROVIDERS.add(0, id -> {
			String source = source(id, ".properties");
			if(source != null) {
				try {
					Properties properties = new Properties();
					properties.load(new StringReader(source));
					Object vert = properties.get("vert");
					Object frag = properties.get("vert");
					Id vertId = vert == null ? id : Id.parse(vert.toString());
					Id fragId = vert == null ? id : Id.parse(frag.toString());
					return new ShaderManager.ShaderPair(vertId, fragId);
				} catch(IOException e) {
					throw Validate.rethrow(e);
				}
			}
			return null;
		});
		ResourceManagerHelper.get(ResourceType.CLIENT_RESOURCES)
			.registerReloadListener(new SimpleSynchronousResourceReloadListener() {
				final Identifier id = new Identifier("jerraria", "shaders");

				@Override
				public void reload(ResourceManager manager) {
					JerrariaClientInit.manager = manager;
					for(Shader<?> shader : ShaderImpl.SHADERS) {
						RenderSystem.recordRenderCall(shader::reload);
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
