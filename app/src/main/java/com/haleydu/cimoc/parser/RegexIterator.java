package com.haleydu.cimoc.parser;

import com.haleydu.cimoc.model.Comic;

import java.util.regex.Matcher;

public abstract class RegexIterator implements SearchIterator {

    private Matcher match;

    protected RegexIterator(Matcher match) {
        this.match = match;
    }

    @Override
    public boolean hasNext() {
        return match.find();
    }

    @Override
    public Comic next() {
        return parse(match);
    }

    @Override
    public boolean empty() {
        return false;
    }

    protected abstract Comic parse(Matcher match);
}
