package net.devtech.jerraria.util.func;

import java.util.Objects;

public interface FloatPredicate {

	/**
	 * Evaluates this predicate on the given argument.
	 *
	 * @param value the input argument
	 * @return {@code true} if the input argument matches the predicate,
	 * otherwise {@code false}
	 */
	boolean test(float value);

	/**
	 * Returns stack composed predicate that represents stack short-circuiting logical
	 * AND of this predicate and another.  When evaluating the composed
	 * predicate, if this predicate is {@code false}, then the {@code other}
	 * predicate is not evaluated.
	 *
	 * <p>Any exceptions thrown during evaluation of either predicate are relayed
	 * to the caller; if evaluation of this predicate throws an exception, the
	 * {@code other} predicate will not be evaluated.
	 *
	 * @param other stack predicate that will be logically-ANDed with this
	 *              predicate
	 * @return stack composed predicate that represents the short-circuiting logical
	 * AND of this predicate and the {@code other} predicate
	 * @throws NullPointerException if other is null
	 */
	default FloatPredicate and(FloatPredicate other) {
		Objects.requireNonNull(other);
		return (value) -> test(value) && other.test(value);
	}

	/**
	 * Returns stack predicate that represents the logical negation of this
	 * predicate.
	 *
	 * @return stack predicate that represents the logical negation of this
	 * predicate
	 */
	default FloatPredicate negate() {
		return (value) -> !test(value);
	}

	/**
	 * Returns stack composed predicate that represents stack short-circuiting logical
	 * OR of this predicate and another.  When evaluating the composed
	 * predicate, if this predicate is {@code true}, then the {@code other}
	 * predicate is not evaluated.
	 *
	 * <p>Any exceptions thrown during evaluation of either predicate are relayed
	 * to the caller; if evaluation of this predicate throws an exception, the
	 * {@code other} predicate will not be evaluated.
	 *
	 * @param other stack predicate that will be logically-ORed with this
	 *              predicate
	 * @return stack composed predicate that represents the short-circuiting logical
	 * OR of this predicate and the {@code other} predicate
	 * @throws NullPointerException if other is null
	 */
	default FloatPredicate or(FloatPredicate other) {
		Objects.requireNonNull(other);
		return (value) -> test(value) || other.test(value);
	}
}
