package net.devtech.jerraria.gui.api;

public abstract class TopState {

	/**
	 * An AutoClosable that when closed, disables the top-ness state of the builder
	 */
	public final PopStack pop;

	protected TopState() {
		this.pop = this::pop;
	}

	protected abstract void pop();

	public interface PopStack extends AutoCloseable {
		@Override
		void close();
	}
}
