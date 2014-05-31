package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 *
 * @author delma
 */
public enum OptionalModifier {
    /**
     * Class dependand instead of instance dependand.
     */
    STATIC,
    /**
     * Javas standard serialization doesn't touch this variable
     */
    TRANSIENT,
    /**
     * This variable wont be cached.
     * Allows use of it in multi threaded system
     */
    VOLATILE,
    /**
     * Can be defined only once.
     */
    FINAL,
    /**
     * Methods only in abstract classes.
     * Will be defined by subclass
     */
    ABSTRACT;

    /**
     * Gets string representation of modifier
     *
     * @return modifier
     */
    public String get() {
        return toString().toLowerCase();
    }
}
