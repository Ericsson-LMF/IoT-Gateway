package com.ericsson.deviceaccess.serviceschema.codegenerator.javabuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Implementation of block of code
 * @author delma
 */
public class CodeBlockImpl implements CodeBlock {
    protected final List<String> lines;

    public CodeBlockImpl() {
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
        CodeBlockImpl codeblock = new CodeBlockImpl();
        block.accept(codeblock);
        codeblock.lines.forEach(s -> add(JavaHelper.INDENT + s));
        add(JavaHelper.BLOCK_END);
        return this;
    }
}
