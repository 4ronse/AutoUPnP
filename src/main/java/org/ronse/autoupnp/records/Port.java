package org.ronse.autoupnp.records;


import de.exlll.configlib.Comment;
import org.jetbrains.annotations.NotNull;
import org.ronse.autoupnp.Protocol;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 *
 * @param ip                Internal IP
 * @param internalPort      Internal Port
 * @param externalPort      External Port
 * @param protocol          {@link Protocol} (Either UDP or TCP)
 * @param description       Short description of service
 * @param disabled          Is disabled
 */
public record Port (String ip,
                    @Comment("Integer between 0-65535") @Min(0) @Max(65535) int internalPort,
                    @Comment("Integer between 0-65535") @Min(0) @Max(65535) int externalPort,
                    @Comment("Either TCP or UDP") Protocol protocol,
                    @Comment("Service description such as \"Minecraft Server\"") String description,
                    boolean disabled
) implements Comparable<Port> {
    @Override
    public boolean equals(Object o) {
        if(!(o instanceof Port other)) return false;

        return this.protocol == other.protocol && (this.internalPort == other.internalPort || this.externalPort == other.externalPort);
    }

    @Override
    public int compareTo(@NotNull Port other) {
        return Integer.compare(this.internalPort, other.internalPort);
    }

    @Override
    public String toString() {
        return String.format("Port[(%s:%d -> %d) %s \"%s\"]", ip, internalPort, externalPort, protocol.toString(), description);
    }
}