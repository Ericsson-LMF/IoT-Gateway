package com.ericsson.deviceaccess.spi.impl;

import java.util.concurrent.Callable;

/**
 *
 * @author delma
 */
public enum LambdaHelper {

    /**
     * Singleton.
     */
    INSTANCE;

    public static <T> T smugle(Callable<T> callable) {
        try {
            return callable.call();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
