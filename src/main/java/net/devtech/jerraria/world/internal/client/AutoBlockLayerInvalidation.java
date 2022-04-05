package net.devtech.jerraria.world.internal.client;

public enum AutoBlockLayerInvalidation {
	/**
	 * Rebuilds the block layer when a neighboring block is updated. Since we don't plan on per-block rebuilds atm,
	 *  this in practice means the block layer quadrant is rebuilt when a neighboring chunk is loaded or
	 *  a border block is changed in a neighboring chunk or neighboring chunk quadrant.
	 */
	ON_NEIGHBOR_BLOCK_UPDATE,

	/**
	 * Rebuilds the block layer when a block inside the quadrant is changed.
	 */
	ON_BLOCK_UPDATE,

	/**
	 * Don't update the block layer automatically, invalidation is handled externally (eg. packet based rendering).
	 */
	NONE;

	public static final AutoBlockLayerInvalidation[] VALUES = AutoBlockLayerInvalidation.values();
}
