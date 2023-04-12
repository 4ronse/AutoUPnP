package org.ronse.autoupnp;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurations;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.*;
import java.util.List;

public final class ConfigHelper {
    public record Port(String ip,
                       @Comment("Integer between 0-65535") int internalPort,
                       @Comment("Integer between 0-65535") int externalPort,
                       @Comment("Either TCP or UDP") Protocol protocol,
                       @Comment("Service description such as \"Minecraft Server\"") String description
    ) {}

    @Configuration
    public static class BaseConfiguration {
        public List<Port> ports = List.of(new Port("0.0.0.0", 25565, 25565, Protocol.TCP, "Minecraft Server"));
    }

    public BaseConfiguration config;
    private final File file = new File(AutoUPnP.instance.getDataFolder(), "ports.yml");

    public ConfigHelper() {

        if(!file.exists()) YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class,
                new BaseConfiguration());

        config = YamlConfigurations.load(file.getAbsoluteFile().toPath(), BaseConfiguration.class);
    }

    public void update() {
        YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class, this.config);
    }
}