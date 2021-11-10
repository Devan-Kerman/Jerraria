package net.devtech.jerraria.util;

import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;
import org.objectweb.asm.Type;

public class Func {
	public static <A> Method get(I1<A> inst) {return getMethod(inst);}

	public static <A, B> Method get(I2<A, B> inst) {return getMethod(inst);}

	public static <A, B, C> Method get(I3<A, B, C> inst) {return getMethod(inst);}

	public static <A, B, C, D> Method get(I4<A, B, C, D> inst) {return getMethod(inst);}

	public static <A, B, C, D, E> Method get(I5<A, B, C, D, E> inst) {return getMethod(inst);}

	public static <A, B, C, D, E, F> Method get(I6<A, B, C, D, E, F> inst) {return getMethod(inst);}

	public static <A, B, C, D, E, F, G> Method get(I7<A, B, C, D, E, F, G> inst) {return getMethod(inst);}

	public static void main(String[] args) {
		System.out.println(Func.get(Func::main));
	}

	@NotNull
	private static Method getMethod(Serializable inst) {
		try {
			Method writeReplace = inst.getClass().getDeclaredMethod("writeReplace");
			writeReplace.setAccessible(true);
			SerializedLambda sl = (SerializedLambda) writeReplace.invoke(inst);
			Class<?> clazz = Class.forName(sl.getImplClass().replace('/', '.'));
			String name = sl.getImplMethodName(), desc = sl.getImplMethodSignature();
			Method m = find(clazz, method -> {
				return method.getName().equals(name) && Type.getMethodDescriptor(method).equals(desc);
			});
			if(m == null) {
				throw new NoSuchMethodException(name + desc);
			} else {
				return m;
			}
		} catch(ReflectiveOperationException e) {
			throw Validate.rethrow(e);
		}
	}

	private static Method find(Class<?> clazz, Predicate<Method> pred) {
		if(clazz == null) {
			return null;
		}

		for(Method method : clazz.getDeclaredMethods()) {
			if(pred.test(method)) {
				return method;
			}
		}

		Method sup = find(clazz.getSuperclass(), pred);
		if(sup != null) {
			return sup;
		}

		for(Class<?> iface : clazz.getInterfaces()) {
			sup = find(iface, pred);
			if(sup != null) {
				return sup;
			}
		}
		return null;
	}

	public interface I1<A> extends Serializable {
		void invoke(A o);
	}

	public interface I2<A, B> extends Serializable {
		void invoke(A o1, B o2);
	}

	public interface I3<A, B, C> extends Serializable {
		void invoke(A o1, B o2, C o3);
	}

	public interface I4<A, B, C, D> extends Serializable {
		void invoke(A o1, B o2, C o3, D o4);
	}

	public interface I5<A, B, C, D, E> extends Serializable {
		void invoke(A o1, B o2, C o3, D o4, E o5);
	}

	public interface I6<A, B, C, D, E, F> extends Serializable {
		void invoke(A o1, B o2, C o3, D o4, E o5, F o6);
	}

	public interface I7<A, B, C, D, E, F, G> extends Serializable {
		void invoke(A o1, B o2, C o3, D o4, E o5, F o6, G o7);
	}
}
