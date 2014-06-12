package com.ericsson.research.commonutil;

import java.util.Enumeration;
import java.util.Iterator;

/**
 *
 * @author aopkarja
 */
public enum LegacyUtil {

    /**
     * Singleton.
     */
    INSTANCE;

    public static <T> Enumeration<T> toEnumeration(Iterator<T> iterator) {
        return new Enumeration<T>() {

            @Override
            public boolean hasMoreElements() {
                return iterator.hasNext();
            }

            @Override
            public T nextElement() {
                return iterator.next();
            }

        };
    }
}
