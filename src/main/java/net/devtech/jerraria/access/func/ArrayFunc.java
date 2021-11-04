package net.devtech.jerraria.access.func;

import java.lang.invoke.SerializedLambda;
import java.lang.reflect.Method;

import com.google.common.reflect.TypeToken;
import org.objectweb.asm.Type;

@SuppressWarnings("UnstableApiUsage")
public interface ArrayFunc<T> {
	T combine(T[] arr);

	default Class<T> getType() {
		// egregious type hacks
		TypeToken<?> token = new TypeToken<T>(this.getClass()) {};
		Class<?> type = token.getRawType();
		if(type == Object.class) { // doesn't work on lambda
			try {
				Method writeReplace = this.getClass().getDeclaredMethod("writeReplace");
				writeReplace.setAccessible(true);
				SerializedLambda sl = (SerializedLambda) writeReplace.invoke(this);
				type = Class.forName(Type.getMethodType(sl.getInstantiatedMethodType()).getReturnType().getClassName());
			} catch(ReflectiveOperationException e) {
			}
		}
		return (Class<T>) type;
	}
}
