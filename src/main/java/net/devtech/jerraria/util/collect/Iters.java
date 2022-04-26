package net.devtech.jerraria.util.collect;

import java.util.Iterator;

public final class Iters {
	public static <T> Iterator<T> of(T value) {
		return new Single<>(value);
	}

	static final class Single<T> implements Iterator<T> {
		final T value;
		boolean visited;

		Single(T value) {
			this.value = value;
		}

		@Override
		public boolean hasNext() {
			return !this.visited;
		}

		@Override
		public T next() {
			this.visited = true;
			return this.value;
		}
	}
}
