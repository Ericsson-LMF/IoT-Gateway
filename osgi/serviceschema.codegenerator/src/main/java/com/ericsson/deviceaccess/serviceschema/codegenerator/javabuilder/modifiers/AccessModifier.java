package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 *
 * @author delma
 */
public enum AccessModifier {

    PUBLIC, PROTECTED, PRIVATE;

    public String get() {
        return toString().toLowerCase();
    }
}
