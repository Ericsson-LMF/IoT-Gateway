package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 * Implementer defines parameters that it can be called with.
 *
 * @author delma
 */
public interface Callable {

    /**
     * Adds Parameter to callable
     *
     * @param parameter parameter to be added
     * @return this
     */
    Callable addParameter(Param parameter);

}
