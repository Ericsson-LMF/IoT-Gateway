package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import static com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder.JavaHelper.STATEMENT_END;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 *
 * @author delma
 */
class AbstractCodeBlock implements CodeBlock {
    protected final List<String> lines;

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
    public CodeBlock addBlock(Object object, Consumer<CodeBlock> block) {
        add(object.toString()).append(" ").append(JavaHelper.BLOCK_START);
        AbstractCodeBlock code = new AbstractCodeBlock();
        block.accept(code);
        code.lines.forEach(s -> add(JavaHelper.INDENT + s));
        add(JavaHelper.BLOCK_END);
        return this;
    }
}
