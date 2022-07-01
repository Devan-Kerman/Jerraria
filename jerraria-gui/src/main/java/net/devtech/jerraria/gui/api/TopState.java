package net.devtech.jerraria.gui.api;

public abstract class TopState {

	/**
	 * An AutoClosable that when closed, disables the top-ness state of the builder
	 */
	public final PopStack self;

	protected TopState() {
		this.self = this::pop;
	}

	protected abstract void pop();

	public interface PopStack extends AutoCloseable {
		@Override
		void close();
	}
}
