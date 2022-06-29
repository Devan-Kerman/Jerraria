package net.devtech.jerraria.util;

public interface SafeClosable extends AutoCloseable {
	@Override
	void close();
}
