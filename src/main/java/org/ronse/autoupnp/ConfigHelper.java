package org.ronse.autoupnp;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;

import java.io.File;
import java.util.List;

public final class ConfigHelper {
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
                       @Comment("Integer between 0-65535") int internalPort,
                       @Comment("Integer between 0-65535") int externalPort,
                       @Comment("Either TCP or UDP") Protocol protocol,
                       @Comment("Service description such as \"Minecraft Server\"") String description,
                       boolean disabled
    ) {
        @Override
        public boolean equals(Object other) {
            if(!(other instanceof Port)) return false;
            return this.internalPort == ((Port) other).internalPort || this.externalPort == ((Port) other).externalPort;
        }
    }

    @Configuration
    public static class BaseConfiguration {
        public List<Port> ports = List.of(new Port("0.0.0.0", 25565, 25565, Protocol.TCP, "Minecraft Server", false));
    }

    public BaseConfiguration config;
    private final File file = new File(AutoUPnP.instance.getDataFolder(), "ports.yml");

    public ConfigHelper() {
        if(!file.exists()) YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class,
                new BaseConfiguration());

        reload();
    }

    public void reload() {
        config = YamlConfigurations.load(file.getAbsoluteFile().toPath(), BaseConfiguration.class);
        AutoUPnP.instance.getComponentLogger().info(Component.text("ports.yml Reloaded").color(TextColor.color(AutoUPnP.COLOR_INFO)));
    }

    public void update() {
        YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class, this.config);
        AutoUPnP.instance.getComponentLogger().info(Component.text("ports.yml Saved").color(TextColor.color(AutoUPnP.COLOR_INFO)));
    }
}