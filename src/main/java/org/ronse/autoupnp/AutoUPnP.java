package org.ronse.autoupnp;

import com.dosse.upnp.UPnP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class AutoUPnP extends JavaPlugin {
    public static AutoUPnP instance;
    public static ConfigHelper configHelper;

    public static final int COLOR_INFO      = 0x0dcaf0;
    public static final int COLOR_WARN      = 0xfecf3e;
    public static final int COLOR_DANGER    = 0xdc3545;

    public static final ArrayList<ConfigHelper.Port> ports = new ArrayList<>();;

    public AutoUPnP() {
        instance = this;
        configHelper = new ConfigHelper();
    }

    @Override
    public void onEnable() {
        openPorts();
    }

    public void openPorts() {
        configHelper.reload();
        configHelper.config.ports.forEach(port -> {
            try {
                getComponentLogger().info(Component.text("Trying to open port " + port.toString()).
                        color(TextColor.color(COLOR_INFO)));
                openPort(port);
                getComponentLogger().info(Component.text(port + " is open").
                        color(TextColor.color(COLOR_INFO)));
            } catch (RuntimeException ex) {
                getComponentLogger().error(Component.text("Failed to open port " + port.toString() +
                        "\n" + ex).color(TextColor.color(COLOR_DANGER)));
            }
        });
    }

    public void openPort(ConfigHelper.Port port) throws RuntimeException {
        if(port.disabled()) {
            getComponentLogger().info(Component.text(port + " is disabled")
                    .color(TextColor.color(COLOR_WARN)));
            return;
        }

        UPnP.waitInit();
        if(!UPnP.defaultGW.openPort(port.ip(), port.internalPort(), port.externalPort(), port.description(),
                port.protocol() == Protocol.UDP)) throw new RuntimeException("Failed to open port!");

        ports.add(port);
    }

    @Override
    public void onDisable() {
        ports.forEach(port -> {
            UPnP.defaultGW.closePort(port.externalPort(), port.protocol() == Protocol.UDP);
            getComponentLogger().info(Component.text(port + " closed")
                    .color(TextColor.color(COLOR_WARN)));
        });

        ports.clear();
    }
}
