package net.devtech.jerraria.util;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import net.minecraft.util.Identifier;

public interface Id extends Comparable<Id> {

	static Id create(String namespace, String path) {
		return (Id) (Object) new Identifier(namespace, path);
	}

	static Id parse(String string) {
		int index = string.indexOf(':');
		if(index == -1) {
			throw new IllegalStateException(string + " is not a valid identifier! must come in form <namespace>:<path>");
		}
		return create(string.substring(0, index), string.substring(index+1));
	}

	static Id from(Identifier id) {
		return (Id) (Object) id;
	}

	String mod();

	String path();

	@Override
	String toString();

	Identifier to();
}
