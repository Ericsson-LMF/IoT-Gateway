package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 * Defines ways of accessing for
 * {@link com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Accessable}.
 *
 * @author delma
 */
public enum AccessModifier {

    /**
     * Can be accessed from anywhere.
     */
    PUBLIC,
    /**
     * Can be accessed from same package or by subclass.
     */
    PROTECTED,
    /**
     * Can be accessed from only class itself.
     */
    PRIVATE;

    /**
     * Gets string representation of modifier
     *
     * @return modifier
     */
    public String get() {
        return toString().toLowerCase();
    }
}
