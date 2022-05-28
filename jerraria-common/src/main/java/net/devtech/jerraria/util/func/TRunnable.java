package net.devtech.jerraria.util.func;

import net.devtech.jerraria.util.Validate;

public interface TRunnable extends Runnable {
	static TRunnable of(TRunnable r) {
		return r;
	}

	@Override
	default void run() {
		try {
			this.exec();
		} catch(Throwable e) {
			Validate.rethrow(e);
		}
	}

	void exec() throws Throwable;
}
