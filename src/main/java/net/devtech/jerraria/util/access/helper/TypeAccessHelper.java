package net.devtech.jerraria.util.access.helper;

import java.lang.reflect.Type;

public class TypeAccessHelper<T, F> extends AbstractClassAccessHelper<T, Type, F> {

	public TypeAccessHelper(HelperContext<Type, F> context) {
		super(context);
	}

	@Override
	protected Type convert(Type type) {
		return type;
	}
}
