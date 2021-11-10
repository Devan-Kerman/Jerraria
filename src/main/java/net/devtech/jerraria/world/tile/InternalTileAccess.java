package net.devtech.jerraria.world.tile;

import net.devtech.jerraria.util.data.element.JCElement;
import net.devtech.jerraria.world.TileLayers;
import net.devtech.jerraria.world.World;
import org.jetbrains.annotations.ApiStatus;

/**
 * @deprecated internal
 */
@Deprecated
@ApiStatus.Internal
public class InternalTileAccess {

	public static JCElement<?> write(TileVariant variant, TileData data) {
		return variant.getOwner().write(data, variant);
	}

	public static TileData read(TileVariant variant, JCElement<?> view) {
		return variant.getOwner().read(variant, view);
	}
}
