package org.ronse.autoupnp.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;

public class AutoUPnPUtil {
    public static Component replace(Component component, String match, String replace) {
        return component.replaceText(TextReplacementConfig.builder().matchLiteral(match).replacement(replace).build());
    }

    public static Component replace(Component component, ReplacementPair... pairs) {
        for(ReplacementPair pair : pairs) component = replace(component, pair.left(), pair.right());
        return component;
    }

    // public static void
}
