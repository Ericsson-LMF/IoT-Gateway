package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.OptionalModifier;

/**
 * Constant {@link Variable} for {@link JavaClass}.
 *
 * @author delma
 */
public class Constant extends Variable {

    /**
     * Creates constant with type, name and value
     *
     * @param type
     * @param name
     * @param value
     */
    public Constant(String type, String name, String value) {
        super(type, name);
        init(value);
        addModifier(OptionalModifier.FINAL);
        addModifier(OptionalModifier.STATIC);
        setAccessModifier(AccessModifier.PUBLIC);
    }

}
