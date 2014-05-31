package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 * Parameter for
 * {@link com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.Callable}
 *
 * @author delma
 */
public class Param {

    private final String name;
    private final String type;
    private String description;

    /**
     * Creates new parameter with type and name
     *
     * @param type
     * @param name
     */
    public Param(String type, String name) {
        this.name = name;
        this.type = type;
    }

    /**
     * Gets name
     *
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets type
     *
     * @return type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets description
     *
     * @param description description
     * @return this
     */
    public Param setDescription(String description) {
        this.description = description;
        return this;
    }

    /**
     * Gets description
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

}
