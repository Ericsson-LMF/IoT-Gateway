package com.ericsson.deviceaccess.serviceschema.codegenerator;

/**
 *
 * @author delma
 */
public enum AccessModifier {

    PUBLIC, PROTECTED, PRIVATE;

    String get() {
        return toString().toLowerCase();
    }
}
