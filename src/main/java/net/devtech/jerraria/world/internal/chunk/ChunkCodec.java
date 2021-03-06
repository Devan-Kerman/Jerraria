package net.devtech.jerraria.world.internal.chunk;

import static com.google.common.collect.Multimaps.asMap;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;
import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntLongImmutablePair;
import it.unimi.dsi.fastutil.ints.IntLongPair;
import it.unimi.dsi.fastutil.ints.IntObjectImmutablePair;
import it.unimi.dsi.fastutil.ints.IntObjectPair;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectObjectImmutablePair;
import net.devtech.jerraria.jerraria.Entities;
import net.devtech.jerraria.jerraria.Tiles;
import net.devtech.jerraria.util.Id;
import net.devtech.jerraria.jerracode.element.JCElement;
import net.devtech.jerraria.jerracode.JCTagView;
import net.devtech.jerraria.jerracode.NativeJCType;
import net.devtech.jerraria.world.World;
import net.devtech.jerraria.world.entity.Entity;
import net.devtech.jerraria.world.entity.EntityInternal;
import net.devtech.jerraria.world.entity.SerializedEntity;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.tile.InternalTileAccess;
import net.devtech.jerraria.world.tile.EnumerableProperty;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;

public class ChunkCodec {
	public static final String RESERVED_ID = "__TILE_ID_INTERNAL__";

	public static void populateTiles(TileVariant[] variants, List<JCTagView> tags) {
		for(int i = 0; i < tags.size(); i++) {
			JCTagView tag = tags.get(i);
			Id full = tag.get(RESERVED_ID, NativeJCType.POOLED_ID);
			Tile tile = Tiles.REGISTRY.getForId(full);
			TileVariant variant = tile.getDefaultVariant();
			for(var entry : tile.getProperties()) {
				String name = entry.getName();
				EnumerableProperty property = entry;
				variant = variant.with(property, property.readFrom(tag.get(name)));
			}
			variants[i] = variant;
		}
	}

	public static List<JCTagView> writeTiles(TileVariant[] variants) {
		List<JCTagView> list = new ArrayList<>();
		for(TileVariant variant : variants) {
			JCTagView.Builder builder = JCTagView.builder();
			Tile owner = variant.getOwner();
			Id.Full id = Tiles.REGISTRY.getId(owner);
			builder.put(RESERVED_ID, NativeJCType.POOLED_ID, id);
			for(EnumerableProperty property : owner.getProperties()) {
				Object value = variant.get(property);
				JCElement convert = property.convert(value);
				builder.put(property.getName(), convert);
			}
			list.add(builder);
		}
		return list;
	}

	public static List<IntObjectPair<JCElement>> serializeData(TileVariant[] variants, Int2ObjectMap<TileData> data) {
		List<IntObjectPair<JCElement>> list = new ArrayList<>();
		for(var entry : data.int2ObjectEntrySet()) {
			int loc = entry.getIntKey();
			TileVariant variant = variants[loc];
			JCElement<?> write = InternalTileAccess.write(variant, entry.getValue());
			list.add(new IntObjectImmutablePair<>(loc, write));
		}
		return list;
	}

	public static Int2ObjectMap<TileData> deserializeData(int cx, int cy, TileVariant[] variants, List<IntObjectPair<JCElement>> view) {
		Int2ObjectMap<TileData> tiles = new Int2ObjectOpenHashMap<>();
		for(IntObjectPair<JCElement> pair : view) {
			JCElement data = pair.second();
			int loc = pair.firstInt();
			TileVariant variant = variants[loc];
			TileData read = InternalTileAccess.read(variant, data);
			InternalTileDataAccess.init(read, loc, cx, cy);
			tiles.put(loc, read);
		}
		return tiles;
	}

	public static Set<Entity> deserializeEntities(World world, List<Pair<Id, List<SerializedEntity>>> entities) {
		Set<Entity> entitySet = new HashSet<>();
		for(var ofTypes : entities) {
			Id id = ofTypes.first();
			for(SerializedEntity entity : ofTypes.second()) {
				Entity deserialized = EntityInternal.deserialize(world, id, entity);
				entitySet.add(deserialized);
			}
		}
		return entitySet;
	}

	public static List<Pair<Id, List<SerializedEntity>>> serializeEntities(Iterable<Entity> entities) {
		ListMultimap<Id, SerializedEntity> map = ArrayListMultimap.create();
		for(Entity entity : entities) {
			if(entity.doesSaveInChunk()) {
				Id id = Entities.REGISTRY.getId(entity.getType());
				SerializedEntity serialize = EntityInternal.serialize(entity);
				map.put(id, serialize);
			}
		}
		List<Pair<Id, List<SerializedEntity>>> list = new ArrayList<>();
		for(var entry : asMap(map).entrySet()) {
			List<SerializedEntity> entityList = entry.getValue();
			list.add(new ObjectObjectImmutablePair<>(entry.getKey(), entityList));
		}
		return list;
	}

	public static List<Pair<Id, JCElement>> serializeTemporaryData(List<UnpositionedTileData> data) {
		List<Pair<Id, JCElement>> elements = new ArrayList<>(data.size());
		for(UnpositionedTileData datum : data) {
			elements.add(new ObjectObjectImmutablePair<>(
				UnpositionedTileData.REGISTRY.getId(datum.getType()),
				((TemporaryTileData.Type)datum.getType()).serialize(datum)
			));
		}
		return elements;
	}

	// might be more effecient to store this as stack Map<Id, List<JCElement>>
	public static List<UnpositionedTileData> deserializeTemporaryData(Chunk chunk, List<Pair<Id, JCElement>> elements) {
		List<UnpositionedTileData> data = new ArrayList<>();
		for(var element : elements) {
			Id id = element.first();
			TemporaryTileData.Type type = UnpositionedTileData.REGISTRY.getForId(id);
			UnpositionedTileData read = type.read(chunk, element.second());
			data.add(read);
		}
		return data;
	}

	public static List<IntLongPair> serializeLinks(Object2IntMap<Chunk> links) {
		List<IntLongPair> list = new ArrayList<>();
		for(Object2IntMap.Entry<Chunk> entry : links.object2IntEntrySet()) {
			list.add(new IntLongImmutablePair(entry.getIntValue(), entry.getKey().getId()));
		}
		return list;
	}

	public static Object2IntMap<Chunk> deserializeLinks(List<IntLongPair> pairs, AbstractWorld world) {
		Object2IntMap<Chunk> chunks = new Object2IntOpenHashMap<>();
		for(IntLongPair pair : pairs) {
			long id = pair.secondLong();
			chunks.put(world.getChunk(Chunk.getA(id), Chunk.getB(id)), pair.firstInt());
		}
		return chunks;
	}
}
