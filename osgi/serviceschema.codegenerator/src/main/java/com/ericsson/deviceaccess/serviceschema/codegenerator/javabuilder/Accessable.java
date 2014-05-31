package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;

/**
 * Implementer defines how it can be accessed.
 *
 * @author delma
 */
public interface Accessable {

    /**
     * How this can be accessed
     *
     * @return AccessModier
     */
    AccessModifier getAccessModifier();

    /**
     * Sets how this can be accessed
     *
     * @param modifier AccessModifier to set
     * @return this
     */
    Accessable setAccessModifier(AccessModifier modifier);

}
