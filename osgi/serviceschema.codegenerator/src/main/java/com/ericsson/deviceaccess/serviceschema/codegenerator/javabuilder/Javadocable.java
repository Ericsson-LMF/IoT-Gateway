package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Javadoc;

/**
 * This can be Javadoced
 *
 * @author delma
 */
public interface Javadocable {

    /**
     * Sets Javadoc for this
     *
     * @param javadoc Javadoc to be set
     * @return this
     */
    Javadocable setJavadoc(Javadoc javadoc);

}
