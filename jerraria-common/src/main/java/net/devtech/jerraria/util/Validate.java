package net.devtech.jerraria.util;

import java.util.Arrays;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import com.sun.tools.javac.Main;
import org.jetbrains.annotations.Nullable;

public class Validate {
	public static final String PROPERTIES_FILE_EXTENSION = "prop";
	public static final boolean IN_DEV = Boolean.getBoolean("jerraria.dev");

	public static void void_(Object object) {}

	public static <T> T orDefault(T val, T def) {
		return val == null ? def : val;
	}

	public static <T> void equals(Msg<T> message, T... objects) {
		for(int ci = 0; ci < objects.length; ci++) {
			T current = objects[ci];
			for(int cmpi = ci; cmpi < objects.length; cmpi++) {
				T compare = objects[cmpi];
				if(!Objects.equals(current, compare)) {
					throw new IllegalArgumentException(message.msg(current, compare));
				}
			}
		}
	}

	/**
	 * @return nothing, because it throws
	 * @throws T rethrows {@code throwable}
	 */
	@SuppressWarnings("unchecked")
	public static <T extends Throwable> RuntimeException rethrow(Throwable throwable) throws T {
		throw (T) throwable;
	}

	/**
	 * @param name the name of the parameter
	 * @return {@code val}
	 * @throws IllegalArgumentException if {@code val} < 0
	 */
	public static int positive(int val, String name) {
		return greaterThanEqualTo(val, 0, name);
	}

	/**
	 * @param name the name of the parameter
	 * @return {@code val}
	 * @throws IllegalArgumentException if {@code val} < {@code comp}
	 */
	public static int greaterThanEqualTo(int val, int comp, String name) {
		if(val >= comp) {
			return val;
		}
		throw new IllegalArgumentException(String.format("%s (%d) < %d!", name, val, comp));
	}

	/**
	 * @param name the name of the parameter
	 * @return {@code val}
	 * @throws IllegalArgumentException if {@code val} <= {@code comp}
	 */
	public static int greaterThan(int val, int comp, String name) {
		if(val > comp) {
			return val;
		}
		throw new IllegalArgumentException(String.format("%s (%d) <= %d!", name, val, comp));
	}

	public static <T> T notNull(T object, String message) {
		if(object == null) {
			throw new IllegalArgumentException(message);
		}
		return object;
	}

	public static <A, B> B instanceOf(A object, Class<B> cls, String message) {
		if(cls.isInstance(object)) {
			return (B) object;
		}
		throw new IllegalArgumentException(message);
	}

	public static <A, B> B filter(A a, Class<B> cls) {
		if(cls.isInstance(a)) {
			return (B) a;
		} else {
			return null;
		}
	}

	@Nullable
	public static <A> A filter(A obj, Predicate<A> a) {
		if(a.test(obj)) {
			return obj;
		} else {
			return null;
		}
	}

	public static void isTrue(boolean va, String msg) {
		if(!va) {
			throw new IllegalArgumentException(msg);
		}
	}

	public static void isNull(Object value, String error) {
		if(value != null) {
			throw new IllegalArgumentException(error);
		}
	}

	public static void lessThan(int index, int length, String s) {
		if(index >= length) {
			throw new IllegalArgumentException(s);
		}
	}

	public static <A, B> B transform(A input, Function<A, B> transform) {
		if(input == null) {
			return null;
		}
		return transform.apply(input);
	}

	public static <A, B, C> C transform(A input, B context, BiFunction<A, B, C> transform) {
		if(input == null) {
			return null;
		}
		return transform.apply(input, context);
	}

	public static IllegalArgumentException invalidArg(String s) {
		throw new IllegalArgumentException(s);
	}

	public static int b2i(boolean bool) {
		return bool ? 1 : 0;
	}

	public static int min(int... a) {
		int smol = Integer.MAX_VALUE;
		for(int i : a) {
			smol = Math.min(smol, i);
		}
		return smol;
	}

	/**
	 * static blocks in interfaces at home
	 */
	public static <T> T create(Supplier<T> supplier) {
		return supplier.get();
	}

	public static void simplifyStackTrace(Throwable origin, Throwable rethrow) {
		StackTraceElement[] current = rethrow.getStackTrace();
		StackTraceElement[] error = origin.getStackTrace();
		for(int i = 0; i < current.length; i++) {
			if(i >= error.length || !error[i].equals(current[i])) {
				origin.setStackTrace(Arrays.copyOfRange(error, i, error.length));
				break;
			}
		}
	}

	public interface Msg<T> {
		String msg(T expected, T value);
	}
}
