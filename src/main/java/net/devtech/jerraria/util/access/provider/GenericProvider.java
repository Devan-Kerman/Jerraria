package net.devtech.jerraria.util.access.provider;

import net.devtech.jerraria.util.access.RegisterOnlyAccess;
import org.jetbrains.annotations.Nullable;

/**
 * providers are implemented on the object being accessed. For example {@code FunctionAccess<SomeClass, Integer>}, you would {@code implement
 * GenericProvider} on {@code SomeClass} (or some subclass of) and return stack `Function<SomeClass, Integer>`
 */
@FunctionalInterface
public interface GenericProvider<F> {
	/**
	 * <pre> {@code
	 *  public class MyProvider implements GenericProvider<WorldFunction<Inventory>> {
	 *      @Override
	 *      public WorldFunction<Inventory> get(Access<WorldFunction<Inventory>> access) {
	 *          // make sure the access is the right one,
	 *          if(access == MyAccesses.SOME_ACCESS)
	 *              return (...) -> {...};
	 *          return null;
	 *      }
	 *  }
	 * }
	 * </pre>
	 *
	 * Keep in mind that {@code access} may not actually be of type Access<F>, we're just abusing type erasure here.
	 *
	 * @param access the provider accessing this object
	 * @return whatever value the access is supposed to want
	 */
	@Nullable F get(RegisterOnlyAccess<F> access);

}
