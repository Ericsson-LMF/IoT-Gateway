package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

/**
 *
 * @author delma
 */
public interface CodeBlock {
    public CodeBlock add(String code);
    public CodeBlock append(Object code);
}
