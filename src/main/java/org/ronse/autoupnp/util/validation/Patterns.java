package org.ronse.autoupnp.util.validation;

import java.util.regex.Pattern;

public class Patterns {
    public static final String IPV4_REGEX = "^(([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.(?!$)|$)){4}$";

    public static final Pattern IPV4_PATTERN = Pattern.compile(IPV4_REGEX);
}
