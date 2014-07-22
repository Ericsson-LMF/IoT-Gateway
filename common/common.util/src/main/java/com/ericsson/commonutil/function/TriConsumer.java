package com.ericsson.commonutil.function;

/**
 *
 * @author delma
 * @param <A>
 * @param <B>
 * @param <C>
 */
@FunctionalInterface
public interface TriConsumer<A, B, C> {

    void consume(A a, B b, C c);

}
