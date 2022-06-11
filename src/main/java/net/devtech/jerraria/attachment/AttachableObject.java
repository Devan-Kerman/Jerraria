package net.devtech.jerraria.attachment;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

import net.devtech.jerraria.util.func.TSupplier;
import net.devtech.jerraria.world.entity.Entity;

public abstract class AttachableObject {
	static final VarHandle HANDLE = TSupplier
		.of(() -> MethodHandles.lookup().findVarHandle(Entity.class, "attachedData", Object[].class)).get();

	Object[] attachedData;
}
