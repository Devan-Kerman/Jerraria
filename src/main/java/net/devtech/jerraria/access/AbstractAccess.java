package net.devtech.jerraria.access;

/**
 * Accesses are events or api providers, they are meant to be flexible and can be circularly-dependent.
 *
 * The common super-interface of {@link RegisterOnlyAccess}, {@link ViewOnlyAccess} and {@link Access}

 * @param <F> A consumer interface of the event, eg. {@code Consumer<MyEvent>}
 */
public interface AbstractAccess<F> {
}
