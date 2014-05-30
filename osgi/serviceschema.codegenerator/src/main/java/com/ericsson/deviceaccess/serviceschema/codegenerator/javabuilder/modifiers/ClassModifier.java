package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 *
 * @author delma
 */
public enum ClassModifier {

    CLASS, INTERFACE, ENUM, SINGLETON, ANNOTATION;

    public String get() {
        if (this == SINGLETON) {
            return ENUM.get();
        }
        return toString().toLowerCase();
    }
}
