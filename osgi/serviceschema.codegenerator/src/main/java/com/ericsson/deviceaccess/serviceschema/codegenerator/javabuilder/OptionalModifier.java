package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 *
 * @author delma
 */
public enum OptionalModifier {

    STATIC, TRANSIENT, VOLATILE, FINAL; 

    String get() {
        return toString().toLowerCase();
    }
}
