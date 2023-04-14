package org.ronse.autoupnp.util;

public class ReplacementPair {
    private final String match;
    private final String replacement;

    public ReplacementPair(String match, String replacement) {
        this.match = match;
        this.replacement = replacement;
    }

    public String left() { return match; }

    public String right() { return replacement; }
}
