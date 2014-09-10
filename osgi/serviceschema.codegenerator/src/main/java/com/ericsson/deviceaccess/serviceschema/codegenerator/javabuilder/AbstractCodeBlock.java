package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.builders.Javadoc;
import com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.modifiers.AccessModifier;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Common implementation of block of code. This is extended to create more
 * specific ones
 *
 * @author delma
 */
public abstract class AbstractCodeBlock implements CodeBlock {

    /**
     * List for code lines
     */
    protected final List<String> lines;

    /**
     * Creates new code block
     */
    public AbstractCodeBlock() {
        lines = new ArrayList<>();
    }

    @Override
    public CodeBlock add(String code) {
        lines.add(code);
        return this;
    }

    @Override
    public CodeBlock append(Object code) {
        int index = lines.size() - 1;
        lines.set(index, lines.get(index) + code);
        return this;
    }

    @Override
    public CodeBlock addBlock(String code, Consumer<CodeBlock> block) {
        add(code).append(" ").append(JavaHelper.BLOCK_START);
        AbstractCodeBlock codeblock = new AbstractCodeBlock() {

            @Override
            public String getName() {
                throw new UnsupportedOperationException("This is anonymous block.");
            }

            @Override
            public String getType() {
                throw new UnsupportedOperationException("This is anonymous block.");
            }

            @Override
            public Javadocable setJavadoc(Javadoc javadoc) {
                throw new UnsupportedOperationException("This is anonymous block.");
            }

            @Override
            public AccessModifier getAccessModifier() {
                throw new UnsupportedOperationException("This is anonymous block.");
            }

            @Override
            public Accessable setAccessModifier(AccessModifier modifier) {
                throw new UnsupportedOperationException("This is anonymous block.");
            }
        };
        block.accept(codeblock);
        codeblock.lines.forEach(s -> add(JavaHelper.INDENT + s));
        add(JavaHelper.BLOCK_END);
        return this;
    }
}
