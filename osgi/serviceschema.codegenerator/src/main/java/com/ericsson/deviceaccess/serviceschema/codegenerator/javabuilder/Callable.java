package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Param;

/**
 * This can be called
 * @author delma
 */
public interface Callable {

    /**
     * Adds Parameter to callable
     * @param parameter parameter to be added
     * @return this
     */
    Callable addParameter(Param parameter);
    
}
