package net.devtech.jerraria.world.item;

import it.unimi.dsi.fastutil.Pair;

public interface ItemMerge {
	Pair<Item.Stack, Item.Stack> tryMerge(Item.Stack a, Item.Stack b);

	record Identity(Item.Stack a, Item.Stack b) implements Pair<Item.Stack, Item.Stack> {
		@Override
		public Item.Stack left() {
			return a;
		}

		@Override
		public Item.Stack right() {
			return b;
		}
	}
}
