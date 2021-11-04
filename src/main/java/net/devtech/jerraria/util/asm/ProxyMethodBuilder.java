package net.devtech.jerraria.util.asm;

import java.util.Set;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;

public interface ProxyMethodBuilder extends Opcodes {
	void emit(ClassNode node);

	void requestFields(Set<FieldPrototype> fields);
}
