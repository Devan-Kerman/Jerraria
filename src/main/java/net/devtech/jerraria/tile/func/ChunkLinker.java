package net.devtech.jerraria.tile.func;

import net.devtech.jerraria.tile.VariantConvertable;
import net.devtech.jerraria.world.ChunkLinkingAccess;

public interface ChunkLinker<T extends VariantConvertable> {
	void link(ChunkLinkingAccess linking, T convertable, int posX, int posY);
}
