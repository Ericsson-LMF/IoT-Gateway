package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers;

/**
 *
 * @author delma
 */
public enum ClassModifier {

    CLASS, INTERFACE, ENUM, SINGLETON, ANNOTATION, ABSTRACT;

    public String get() {
        if (this == SINGLETON) {
            return ENUM.get();
        }
        if(this == ABSTRACT){
            return "abstract class";
        }
        return toString().toLowerCase();
    }
}
