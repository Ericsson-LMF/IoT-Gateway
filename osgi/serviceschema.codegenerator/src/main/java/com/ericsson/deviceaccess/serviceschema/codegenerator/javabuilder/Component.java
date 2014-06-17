package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 * Implementer generates code component.
 *
 * @author delma
 */
public interface Component extends Javadocable, Accessable {

    /**
     * Gets name of the component
     *
     * @return name
     */
    String getName();

    /**
     * Gets type of the component
     *
     * @return type
     */
    String getType();

}
