package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 *
 * @author delma
 */
public enum ClassModifier {

    CLASS, INTERFACE, ENUM, SINGLETON, ANNOTATION;

    String get() {
        if (this == SINGLETON) {
            return ENUM.get();
        }
        return toString().toLowerCase();
    }
}
