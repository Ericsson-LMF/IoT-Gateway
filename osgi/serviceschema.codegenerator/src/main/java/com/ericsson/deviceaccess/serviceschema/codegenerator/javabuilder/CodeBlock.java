package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.function.Consumer;

/**
 * Implementer generates block of code.
 *
 * @author delma
 */
public interface CodeBlock extends Component {

    /**
     * Adds line of code
     *
     * @param code code to be added
     * @return this
     */
    CodeBlock add(String code);

    /**
     * Appends to last line of code
     *
     * @param code code to be appended
     * @return this
     */
    CodeBlock append(Object code);

    /**
     * Adds whole block of code
     *
     * @param code code before block starting
     * @param block consumer to add block code in
     * @return this
     */
    CodeBlock addBlock(String code, Consumer<CodeBlock> block);
}
