package net.devtech.jerraria.world.internal;

import java.util.ArrayList;
import java.util.List;

import net.devtech.jerraria.content.Tiles;
import net.devtech.jerraria.registry.Id;
import net.devtech.jerraria.util.data.JCElement;
import net.devtech.jerraria.util.data.JCTagView;
import net.devtech.jerraria.util.data.NativeJCType;
import net.devtech.jerraria.world.tile.Property;
import net.devtech.jerraria.world.tile.Tile;
import net.devtech.jerraria.world.tile.TileVariant;

public class ChunkIOUtil {
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
}
