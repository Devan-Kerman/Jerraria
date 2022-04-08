package net.devtech.jerraria.world.internal.client;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import com.google.common.collect.Iterables;
import net.devtech.jerraria.render.api.Primitive;
import net.devtech.jerraria.render.api.Shader;
import net.devtech.jerraria.jerracode.JCTagView;
import net.devtech.jerraria.util.math.Matrix3f;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.BaseEntity;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.internal.chunk.Chunk;
import net.devtech.jerraria.world.internal.chunk.ChunkCodec;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;
import net.devtech.jerraria.world.tile.render.AutoBlockLayerInvalidation;
import net.devtech.jerraria.world.tile.render.ShaderSource;
import org.jetbrains.annotations.Nullable;


/**
 * Terminology:
 * shader layer refers to a specific gl shader with specific uniforms (and not a TileLayer)
 */
public class ClientChunk extends Chunk {
	static final Executor EXECUTOR = Executors.newFixedThreadPool(Math.min(Runtime.getRuntime().availableProcessors(), 4));

	final @Nullable BakedClientChunkQuadrant[] quadrants = new BakedClientChunkQuadrant[4];
	final @Nullable Future<?>[] futures = new Future[4];

	public ClientChunk(AbstractWorld world, int chunkX, int chunkY) {
		super(world, chunkX, chunkY);
	}

	public ClientChunk(AbstractWorld world, int chunkX, int chunkY, JCTagView tag) {
		super(world, chunkX, chunkY, tag);
	}

	public ClientChunk(ClientChunk client) {
		super(client.world, client.chunkX, client.chunkY);
		System.arraycopy(client.variants, 0, this.variants, 0, this.variants.length);
		var view = ChunkCodec.serializeData(client.variants, client.data);
		var data = ChunkCodec.deserializeData(client.chunkX, client.chunkY, client.variants, view);
		this.data.putAll(data);

		var entityData = ChunkCodec.serializeEntities(client.entities);
		Set<BaseEntity> entities = ChunkCodec.deserializeEntities(this.getWorld(), entityData);
		this.entities.addAll(entities);
	}

	void scheduleQuadrantReRender(int quadrantX, int quadrantY, AutoBlockLayerInvalidation reason) {
		int quadrantIndex = quadrantX * 2 + quadrantY;
		BakedClientChunkQuadrant quadrant = this.quadrants[quadrantIndex];
		int absQuadX = quadrantX + this.chunkX * 2, absQuadY = quadrantY + this.chunkY * 2;
		if (quadrant == null || quadrant.minInvalidation.ordinal() < reason.ordinal()) {
			ClientChunk[] cache = new ClientChunk[4];
			int cacheX = (absQuadX - 1) >> 1, cacheY = (absQuadY - 1) >> 1;
			if (reason == AutoBlockLayerInvalidation.ON_BLOCK_UPDATE) {
				for (int cqx = -1; cqx <= 1; cqx++) {
					for (int cqy = -1; cqy <= 1; cqy++) {
						if (cqx != 0 && cqy != 0) {
							int ucqx = cqx + absQuadX, ucqy = cqy + absQuadY;
							int ucx = ucqx >> 1, ucy = ucqy >> 1;
							boolean loaded = this.world.isChunkLoaded(ucx, ucy);
							if (loaded) {
								Chunk chunk = this.world.getChunk(ucx, ucy);
								cache[(ucx - cacheX) * 2 + (ucy - cacheY)] = (ClientChunk) chunk;
								((ClientChunk) chunk).scheduleQuadrantReRender(ucqx & 1,
									ucqy & 1,
									AutoBlockLayerInvalidation.ON_NEIGHBOR_BLOCK_UPDATE
								);
							}
						}
					}
				}
			}

			for (int off = 0; off < cache.length; off++) {
				if (cache[off] == null) {
					int x = off / 2, y = off % 2;
					cache[off] = (ClientChunk) this.world.getChunk(x, y);
					if (cache[off] == null) { // cannot guarantee render
						return;
					}
				}
			}

			for (int i = 0; i < cache.length; i++) {
				cache[i] = new ClientChunk(cache[i]);
			}

			Future<?> future = this.futures[quadrantIndex];
			if(future != null) {
				future.cancel(true);
			}
			this.futures[quadrantIndex] = CompletableFuture.runAsync(() -> {
				LocalClientWorldSnapshot snapshot = new LocalClientWorldSnapshot(
					this.world.getServer(),
					this.world.sessionId(),
					cacheX,
					cacheY,
					cache
				);

				var baked = ClientChunkBakedTileQuadrantRenderer.bake(
					snapshot,
					absQuadX,
					absQuadY
				);

				this.quadrants[quadrantIndex] = baked;
				this.futures[quadrantIndex] = null;
			}, EXECUTOR);
		}
	}

	@Override
	public TileData set(TileLayers layer, int x, int y, TileVariant value) {
		int quadX = x >> World.LOG2_CHUNK_QUADRANT_SIZE, quadY = y >> World.LOG2_CHUNK_QUADRANT_SIZE;
		TileData set = super.set(layer, x, y, value);
		this.scheduleQuadrantReRender(quadX, quadY, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
		return set;
	}

	@Override
	public TileData setData(TileLayers layers, int x, int y, TileData data) {
		int quadX = x >> World.LOG2_CHUNK_QUADRANT_SIZE, quadY = y >> World.LOG2_CHUNK_QUADRANT_SIZE;
		TileData data1 = super.setData(layers, x, y, data);
		this.scheduleQuadrantReRender(quadX, quadY, AutoBlockLayerInvalidation.ON_BLOCK_UPDATE);
		return data1;
	}

	@SuppressWarnings({
		"unchecked",
		"rawtypes"
	})
	public void render(Matrix3f chunkMatrix) {
		@Nullable BakedClientChunkQuadrant[] chunkQuadrants = this.quadrants;
		for(int i = 0, length = chunkQuadrants.length; i < length; i++) {
			BakedClientChunkQuadrant quadrant = chunkQuadrants[i];
			if(quadrant == null) {
				continue;
			}
			int x = i / 2, y = i % 2;
			Matrix3f copy = chunkMatrix.copy().offset(x << World.LOG2_CHUNK_QUADRANT_SIZE, y << World.LOG2_CHUNK_QUADRANT_SIZE);
			for(BakedClientChunkQuadrantData layer : quadrant.opaqueLayers) {
				layer.configurator.configureUniforms(copy, layer.vertexData);
				layer.vertexData.render(Primitive.TRIANGLE);
			}
		}
	}

	record BakedClientChunkQuadrantData<T extends Shader<?>>(AutoBlockLayerInvalidation invalidation, T vertexData,
															 ShaderSource.ShaderConfigurator<T> configurator, Primitive primitive) {
	}

	@SuppressWarnings("rawtypes")
	static final class BakedClientChunkQuadrant {
		final AutoBlockLayerInvalidation minInvalidation;
		/**
		 * The vertex & uniforms of a single shader layer. The shader must be used to render opaque objects because these are
		 * "sorted" per-layer rather than per-vertex.
		 */
		final List<BakedClientChunkQuadrantData> opaqueLayers;

		/**
		 * A primitive or multiple primitives of a translucent object in one quadrant of a ClientChunk's baked model.
		 * If multiple primitives of the same shader are all rendered in succession they can be condensed down into a common
		 * primitive set. This class represents that set.
		 */
		final List<BakedClientChunkQuadrantData> sortedTranslucentCommonPrimitiveSets;

		BakedClientChunkQuadrant(List<BakedClientChunkQuadrantData> layers, List<BakedClientChunkQuadrantData> sets) {
			this.opaqueLayers = layers;
			this.sortedTranslucentCommonPrimitiveSets = sets;
			int minInvalidation = AutoBlockLayerInvalidation.NONE.ordinal();
			for (BakedClientChunkQuadrantData data : Iterables.concat(layers, sets)) {
				int ordinal = data.invalidation.ordinal();
				if (ordinal < minInvalidation) {
					minInvalidation = ordinal;
				}
				if (ordinal == 0) {
					break;
				}
			}
			this.minInvalidation = AutoBlockLayerInvalidation.VALUES[minInvalidation];
		}
	}

	// todo custom client chunk quadrant data
}
