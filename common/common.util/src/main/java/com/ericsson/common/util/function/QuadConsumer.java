package com.ericsson.common.util.function;

/**
 *
 * @author delma
 * @param <A>
 * @param <B>
 * @param <C>
 * @param <D>
 */
@FunctionalInterface
public interface QuadConsumer<A, B, C, D> {

    void consume(A a, B b, C c, D d);

}
