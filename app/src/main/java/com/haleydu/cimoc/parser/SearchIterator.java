package com.haleydu.cimoc.parser;

import com.haleydu.cimoc.model.Comic;

public interface SearchIterator {

    boolean empty();

    boolean hasNext();

    Comic next();
}
