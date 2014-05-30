package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 *
 * @author delma
 */
public enum OptionalModifier {

    STATIC, TRANSIENT, VOLATILE, FINAL, ABSTRACT;

    public String get() {
        return toString().toLowerCase();
    }
}
