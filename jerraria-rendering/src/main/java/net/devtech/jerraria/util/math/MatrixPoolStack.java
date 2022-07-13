package net.devtech.jerraria.util.math;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public final class MatrixPoolStack {
	final List<MatrixPool> unused;
	final AtomicInteger active = new AtomicInteger();

	public MatrixPoolStack(int initialSize) {
		this.unused = new ArrayList<>(100);
		for(int i = 0; i < initialSize; i++) {
			this.unused.add(new MatrixPool());
		}
	}

	MatrixPool pop() {
		List<MatrixPool> unused = this.unused;
		if(unused.isEmpty()) {
			this.active.incrementAndGet();
			return new MatrixPool();
		} else {
			this.active.incrementAndGet();
			return unused.remove(unused.size()-1);
		}
	}

	public MatCacheEntry identity(MatType type) {
		MatrixPool pop = this.pop();
		return new Entry(pop, pop.identity(type));
	}

	public MatCacheEntry copy(MatView view) {
		MatrixPool pop = this.pop();
		return new Entry(pop, pop.copy(view));
	}

	public void assertEmpty() {
		if(this.active.get() != 0) {
			throw new IllegalStateException("Pool leak!");
		}
	}

	class Entry implements MatCacheEntry {
		final MatrixPool cache;
		final Mat instance;
		boolean closed;

		Entry(MatrixPool cache, Mat instance) {
			this.cache = cache;
			this.instance = instance;
		}

		@Override
		public Mat instance() {
			return this.instance;
		}

		@Override
		public synchronized void close() {
			if(this.closed) {
				throw new IllegalStateException();
			}
			this.closed = true;
			MatrixPoolStack.this.unused.add(this.cache);
			MatrixPoolStack.this.active.decrementAndGet();
		}
	}
}
