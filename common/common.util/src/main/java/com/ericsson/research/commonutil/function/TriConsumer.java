package com.ericsson.research.commonutil.function;

/**
 *
 * @author delma
 * @param <A>
 * @param <B>
 * @param <C>
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {

    public void consume(A a, B b, C c);

}
