package org.ronse.autoupnp;

public enum Protocol {
    TCP, UDP;

    public static Protocol fromString(String string) {
        if(string.equalsIgnoreCase("udp")) return UDP;
        return TCP;
    }
}
