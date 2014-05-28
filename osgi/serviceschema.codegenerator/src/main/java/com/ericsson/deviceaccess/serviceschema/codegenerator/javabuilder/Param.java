package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 *
 * @author delma
 */
public class Param {

    private final String name;
    private final String type;
    private String description;

    public Param(String type, String name) {
        this.name = name;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public String getType() {
        return type;
    }

    public Param setDescription(String description) {
        this.description = description;
        return this;
    }

    public String getDescription() {
        return description;
    }

}
