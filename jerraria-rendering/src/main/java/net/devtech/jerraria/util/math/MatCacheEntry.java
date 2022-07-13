package net.devtech.jerraria.util.math;

public interface MatCacheEntry extends AutoCloseable {
	Mat instance();

	/**
	 * Returns the object to the cache
	 */
	@Override
	void close();
}
