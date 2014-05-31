package com.ericsson.deviceaccess.spi.utility;

/**
 *
 * @author delma
 * @param <A>
 * @param <B>
 * @param <C>
 */
public interface TriConsumer<A, B, C> {

    public void consume(A a, B b, C c);
    
}
