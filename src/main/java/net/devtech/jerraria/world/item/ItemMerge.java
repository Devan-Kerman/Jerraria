package net.devtech.jerraria.world.item;

import it.unimi.dsi.fastutil.Pair;

public interface ItemMerge {
	Identity IDENTITY = new Identity(null, null);

	Pair<Item.Stack, Item.Stack> tryMerge(Item.Stack a, Item.Stack b);

	default Pair<Item.Stack, Item.Stack> tryMergeSafe(Item.Stack a, Item.Stack b) {
		Pair<Item.Stack, Item.Stack> pair = this.tryMerge(a, b);
		if(pair == IDENTITY) {
			return new Identity(a, b);
		} else {
			return pair;
		}
	}

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
