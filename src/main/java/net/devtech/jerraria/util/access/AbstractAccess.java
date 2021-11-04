package net.devtech.jerraria.util.access;

import net.devtech.jerraria.util.access.helper.AccessContext;

/**
 * Accesses are events or api providers, they are meant to be flexible and can be circularly-dependent.
 *
 * The common super-interface of {@link RegisterOnlyAccess}, {@link ViewOnlyAccess} and {@link Access}

 * @param <F> A consumer interface of the event, eg. {@code Consumer<MyEvent>}
 */
public interface AbstractAccess<F> extends AccessContext<F> {
}
