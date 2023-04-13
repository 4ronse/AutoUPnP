package org.ronse.autoupnp;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.ronse.autoupnp.records.Port;

import java.io.File;
import java.util.List;

public final class ConfigHelper {
    private static ConfigHelper instance;

    @Configuration
    public static class BaseConfiguration {
        public List<Port> ports = List.of(new Port("0.0.0.0", 25565, 25565, Protocol.TCP, "Minecraft Server", false));
    }

    public BaseConfiguration config;
    private final File file = new File(AutoUPnP.instance.getDataFolder(), "ports.yml");

    private ConfigHelper() {
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

    public static ConfigHelper getInstance() {
        if(instance == null) instance = new ConfigHelper();
        return instance;
    }

    public static BaseConfiguration getConfig() {
        return getInstance().config;
    }
}