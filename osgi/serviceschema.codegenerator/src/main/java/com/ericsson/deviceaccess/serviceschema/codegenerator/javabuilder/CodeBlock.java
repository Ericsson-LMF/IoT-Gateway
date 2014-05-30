package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.function.Consumer;

/**
 *
 * @author delma
 */
public interface CodeBlock {

    public CodeBlock add(String code);

    public CodeBlock append(Object code);

    public CodeBlock addBlock(Object object, Consumer<CodeBlock> block);
}
