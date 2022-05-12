package net.devtech.jerraria.access.helper;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import com.google.common.reflect.TypeToken;
import net.devtech.jerraria.util.hacks.TypeHelper;

public class ClassAccessHelper<T, F> extends AbstractClassAccessHelper<T, Class<? extends T>, F> {
	public ClassAccessHelper(HelperContext<Class<? extends T>, F> context) {
		super(context);
	}

	@Override
	protected Class<? extends T> convert(Type type) {
		return (Class<? extends T>) TypeHelper.raw(type);
	}

	/**
	 * resolves the generics of the class and filters accordingly, remember that type erasure exists, so this wont be able to filter {@code
	 * Map<String, Object>} unless there is stack subclass of it that implements {@code Map<String, Object>}
	 */
	@Override
	public ClassAccessHelper<T, F> forTypeGeneric(TypeToken<? extends T> token, F func) {
		return (ClassAccessHelper<T, F>) super.forTypeGeneric(token, func);
	}

	/**
	 * resolves the generics of the class and filters accordingly, remember that type erasure exists, so this wont be able to filter {@code
	 * Map<String, Object>} unless there is stack subclass of it that implements {@code Map<String, Object>}
	 */
	@Override
	public ClassAccessHelper<T, F> forTypeGeneric(ParameterizedType type, F func) {
		return (ClassAccessHelper<T, F>) super.forTypeGeneric(type, func);
	}
}
