package com.scrawl;

import org.apache.commons.lang3.tuple.Pair;

import java.io.IOException;
import java.util.Set;

public interface Parser {

    /**
     * Parses a url and returns the links found
     * @param url URL to fetch and parse
     * @return Pair of input url and links (converted to absolute links) on the page
     */
    Pair<String, Set<String>> parse(final String url) throws IOException;
}
