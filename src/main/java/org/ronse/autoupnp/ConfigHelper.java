package org.ronse.autoupnp;

import de.exlll.configlib.Configuration;
import de.exlll.configlib.YamlConfigurations;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.ronse.autoupnp.records.Port;

import java.io.File;
import java.net.Inet4Address;
import java.net.UnknownHostException;
import java.util.List;

public final class ConfigHelper {
    public static final TextComponent CONFIG_RELOADED   = AutoUPnP.PREFIX.append(Component.text("ports.yml Reloaded").color(TextColor.color(AutoUPnP.COLOR_INFO)));
    public static final TextComponent CONFIG_SAVED      = AutoUPnP.PREFIX.append(Component.text("ports.yml Saved").color(TextColor.color(AutoUPnP.COLOR_INFO)));

    private static ConfigHelper instance;

    @Configuration
    public static class BaseConfiguration {
        public List<Port> ports;

        public BaseConfiguration() {
            String ip;
            try {
                ip = Inet4Address.getLocalHost().getHostAddress();
            } catch (UnknownHostException ex) {
                ip = "192.168.1.100";
            }

            ports = List.of(new Port(ip, Bukkit.getServer().getPort(), Bukkit.getServer().getPort(), Protocol.TCP, "Minecraft Server", false));
        }
    }

    public BaseConfiguration config;
    private final File file = new File(AutoUPnP.instance.getDataFolder(), "ports.yml");

    private ConfigHelper() {
        reload();
    }

    public void reload() {
        if(!file.exists()) YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class,
                new BaseConfiguration());

        config = YamlConfigurations.load(file.getAbsoluteFile().toPath(), BaseConfiguration.class);
        AutoUPnP.instance.getComponentLogger().info(CONFIG_RELOADED);
    }

    public void update() {
        YamlConfigurations.save(file.getAbsoluteFile().toPath(), BaseConfiguration.class, this.config);
        AutoUPnP.instance.getComponentLogger().info(CONFIG_SAVED);
    }

    public static ConfigHelper getInstance() {
        if(instance == null) instance = new ConfigHelper();
        return instance;
    }

    public static BaseConfiguration getConfig() {
        return getInstance().config;
    }
}