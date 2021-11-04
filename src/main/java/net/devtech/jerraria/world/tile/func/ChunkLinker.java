package net.devtech.jerraria.world.tile.func;

import net.devtech.jerraria.world.tile.VariantConvertable;
import net.devtech.jerraria.world.chunk.ChunkLinkingAccess;

public interface ChunkLinker<T extends VariantConvertable> {
	void link(ChunkLinkingAccess linking, T convertable, int posX, int posY);
}
