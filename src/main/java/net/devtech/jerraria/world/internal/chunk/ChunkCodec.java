package net.devtech.jerraria.world.internal.chunk;

import java.util.ArrayList;
import java.util.List;

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
import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.world.internal.AbstractWorld;
import net.devtech.jerraria.world.tile.InternalTileAccess;
import net.devtech.jerraria.world.tile.Property;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileData;
import net.devtech.jerraria.world.tile.TileVariant;

public class ChunkCodec {
	public static final String RESERVED_ID = "__TILE_ID_INTERNAL__";

	public static void populateTiles(TileVariant[] variants, List<JCTagView> tags) {
		for(int i = 0; i < tags.size(); i++) {
			JCTagView tag = tags.get(i);
			Id.Full full = tag.get(RESERVED_ID, NativeJCType.POOLED_PACKED_ID);
			Tile tile = Tiles.REGISTRY.getForId(full);
			TileVariant variant = tile.getDefaultVariant();
			for(var entry : tile.getProperties()) {
				String name = entry.getName();
				Property property = entry;
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
			builder.put(RESERVED_ID, NativeJCType.POOLED_PACKED_ID, id);
			for(Property property : owner.getProperties()) {
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

	public static List<Pair<Id.Full, JCElement>> serializeTemporaryData(List<UnpositionedTileData> data) {
		List<Pair<Id.Full, JCElement>> elements = new ArrayList<>(data.size());
		for(UnpositionedTileData datum : data) {
			elements.add(new ObjectObjectImmutablePair<>(
				UnpositionedTileData.REGISTRY.getId(datum.getType()),
				((TemporaryTileData.Type)datum.getType()).serialize(datum)
			));
		}
		return elements;
	}

	// might be more effecient to store this as a Map<Id, List<JCElement>>
	public static List<UnpositionedTileData> deserializeTemporaryData(Chunk chunk, List<Pair<Id.Full, JCElement>> elements) {
		List<UnpositionedTileData> data = new ArrayList<>();
		for(var element : elements) {
			Id.Full id = element.first();
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
