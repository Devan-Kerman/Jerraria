package net.devtech.jerraria.util.func;

import net.devtech.jerraria.tile.VariantConvertable;
import net.devtech.jerraria.world.chunk.ChunkLinkingAccess;

public interface TileLinker<T extends VariantConvertable> {
	void link(ChunkLinkingAccess linking, T convertable, int posX, int posY);
}
