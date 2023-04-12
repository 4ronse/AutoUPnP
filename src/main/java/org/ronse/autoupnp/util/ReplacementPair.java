package org.ronse.autoupnp.util;

import it.unimi.dsi.fastutil.Pair;

public class ReplacementPair implements Pair<String, String> {
    private final String match;
    private final String replacement;

    public ReplacementPair(String match, String replacement) {
        this.match = match;
        this.replacement = replacement;
    }

    @Override
    public String left() { return match; }

    @Override
    public String right() { return replacement; }
}
