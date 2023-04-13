package org.ronse.autoupnp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.ronse.autoupnp.commands.*;
import org.ronse.autoupnp.util.AutoUPnPUtil;

import java.util.List;

public final class AutoUPnP extends JavaPlugin {
    public static AutoUPnP instance;

    public static final int COLOR_INFO      = 0x0dcaf0;
    public static final int COLOR_SUCCESS   = 0x005E0C;
    public static final int COLOR_WARN      = 0xfecf3e;
    public static final int COLOR_DANGER    = 0xdc3545;

    public static final TextComponent PREFIX;
    public static final TextComponent FAILED_TO_EXECUTE_COMMAND;
    public static final TextComponent PORT_OPEN_ALREADY;
    public static final TextComponent PORT_OPEN_SUCCESS;
    public static final TextComponent PORT_OPEN_FAILURE;
    public static final TextComponent PORT_CLOSE_SUCCESS;
    public static final TextComponent PORT_CLOSE_FAILURE;
    public static final TextComponent PORT_DISABLED;

    public static final TextComponent ON_DISABLE_CLOSE;

    static {
        PREFIX = Component.text("[").color(TextColor.color(COLOR_WARN))
                .append(Component.text("AutoUPnP").color(TextColor.color(COLOR_INFO)))
                .append(Component.text("]").color(TextColor.color(COLOR_WARN)))
                .append(Component.space());

        FAILED_TO_EXECUTE_COMMAND       = PREFIX.append(Component.text("Failed to execute <cmd>").color(TextColor.color(COLOR_DANGER)))
                .append(Component.newline()).append(PREFIX).append(Component.text("<err>").color(TextColor.color(COLOR_DANGER)));

        PORT_OPEN_ALREADY               = PREFIX.append(Component.text("<port> is open already!").color(TextColor.color(COLOR_WARN)));
        PORT_OPEN_SUCCESS               = PREFIX.append(Component.text("<port> is open").color(TextColor.color(COLOR_SUCCESS)));
        PORT_OPEN_FAILURE               = PREFIX.append(Component.text("Can't open port. <err>").color(TextColor.color(COLOR_DANGER)));
        PORT_CLOSE_SUCCESS              = PREFIX.append(Component.text("<port> closed successfully").color(TextColor.color(COLOR_SUCCESS)));
        PORT_CLOSE_FAILURE              = PREFIX.append(Component.text("Can't close port. <err>").color(TextColor.color(COLOR_DANGER)));
        PORT_DISABLED                   = PREFIX.append(Component.text("<port> is disabled, thus won't be forwarded").color(TextColor.color(COLOR_WARN)));

        ON_DISABLE_CLOSE                = PREFIX.append(Component.text("<port> closed").color(TextColor.color(COLOR_WARN)));
    }

    public AutoUPnP() {
        instance = this;
        PortHelper.initialize();
    }

    public void registerCommands() {
        new OpenPort();
        new ClosePort();
        new ListPorts();
        new ReloadConfig();
    }

    public void openAllPorts() {
        if(PortHelper.openPorts(List.copyOf(ConfigHelper.getConfig().ports)) == PortHelper.RESULT_SUCCESS) return;
        getComponentLogger().error(AutoUPnPUtil.replace(PORT_OPEN_FAILURE, "<err>", PortHelper.getLastErrorMessage()));
    }

    public void closeAllPorts() {
        if(PortHelper.closePorts(List.copyOf(PortHelper.allPorts())) == PortHelper.RESULT_SUCCESS) return;
        getComponentLogger().error(AutoUPnPUtil.replace(PORT_CLOSE_FAILURE, "<err>", PortHelper.getLastErrorMessage()));
    }

    @Override
    public void onEnable() {
        openAllPorts();
        registerCommands();
    }

    @Override
    public void onDisable() {
        closeAllPorts();
    }
}
