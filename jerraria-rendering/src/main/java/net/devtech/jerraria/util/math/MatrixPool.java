package net.devtech.jerraria.util.math;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.ConcurrentModificationException;

import net.devtech.jerraria.util.Validate;

public final class MatrixPool {
	static final VarHandle LOCK;

	static {
		try {
			LOCK = MethodHandles
				       .privateLookupIn(MatrixPool.class, MethodHandles.lookup())
				       .findVarHandle(MatrixPool.class, "hasLock", boolean.class);
		} catch(NoSuchFieldException | IllegalAccessException e) {
			throw Validate.rethrow(e);
		}
	}

	final Mat[] mats = new Mat[MatType.ids()];
	volatile boolean hasLock;

	public Mat identity(MatType type) {
		if(LOCK.compareAndSet(this, false, true)) {
			try {
				Mat mat = this.mats[type.typeId];
				if(mat == null) {
					return this.mats[type.typeId] = type.createIdentity();
				} else {
					return mat.identity();
				}
			} finally {
				LOCK.setVolatile(this, false);
			}
		} else {
			throw new ConcurrentModificationException("Cannot use MatrixCache on multiple threads!");
		}
	}

	public Mat copy(MatView view) {
		if(!Validate.IN_DEV || LOCK.compareAndSet(this, false, true)) {
			MatType type = view.getType();
			try {
				Mat mat = this.mats[type.typeId];
				if(mat == null) {
					return this.mats[type.typeId] = view.copy();
				} else {
					return mat.load(view);
				}
			} finally {
				if(Validate.IN_DEV) {
					LOCK.setVolatile(this, false);
				}
			}
		} else {
			throw new ConcurrentModificationException("Cannot use MatrixCache on multiple threads!");
		}
	}
}
