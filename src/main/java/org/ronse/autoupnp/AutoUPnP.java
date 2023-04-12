package org.ronse.autoupnp;

import com.dosse.upnp.UPnP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.ronse.autoupnp.commands.OpenPort;
import org.ronse.autoupnp.exceptions.AutoUPnPPortDisabledException;
import org.ronse.autoupnp.util.AutoUPnPUtil;
import org.ronse.autoupnp.util.ReplacementPair;

import java.util.ArrayList;

public final class AutoUPnP extends JavaPlugin {
    public static AutoUPnP instance;
    public static ConfigHelper configHelper;

    public static final int COLOR_INFO      = 0x0dcaf0;
    public static final int COLOR_SUCCESS   = 0x005E0C;
    public static final int COLOR_WARN      = 0xfecf3e;
    public static final int COLOR_DANGER    = 0xdc3545;

    public static final ArrayList<ConfigHelper.Port> ports = new ArrayList<>();

    public static final TextComponent PREFIX;
    public static final TextComponent OPEN_PORTS_OPEN;
    public static final TextComponent OPEN_PORTS_TRY;
    public static final TextComponent OPEN_PORTS_SUCCESS;
    public static final TextComponent OPEN_PORTS_DISABLED;
    public static final TextComponent OPEN_PORTS_FAILED;

    public static final TextComponent ON_DISABLE_CLOSE;

    static {
        PREFIX = Component.text("[").color(TextColor.color(COLOR_WARN))
                .append(Component.text("AutoUPnP").color(TextColor.color(COLOR_INFO)))
                .append(Component.text("]").color(TextColor.color(COLOR_WARN)))
                .append(Component.space());

        OPEN_PORTS_OPEN = PREFIX.append(Component.text("<port> is open already!").color(TextColor.color(COLOR_INFO)));
        OPEN_PORTS_TRY = PREFIX.append(Component.text("Trying to open <port>").color(TextColor.color(COLOR_INFO)));
        OPEN_PORTS_SUCCESS = PREFIX.append(Component.text("<port> is open").color(TextColor.color(COLOR_SUCCESS)));
        OPEN_PORTS_DISABLED = PREFIX.append(Component.text("<port> is disabled, thus won't be forwarded").color(TextColor.color(COLOR_WARN)));
        OPEN_PORTS_FAILED = PREFIX.append(Component.text("Failed to open <port> \n<ex>").color(TextColor.color(COLOR_DANGER)));

        ON_DISABLE_CLOSE = PREFIX.append(Component.text("<port> closed").color(TextColor.color(COLOR_WARN)));
    }

    public AutoUPnP() {
        instance = this;
        configHelper = new ConfigHelper();
    }

    @Override
    public void onEnable() {
        openPorts();
        registerCommands();
    }

    public void registerCommands() {
        new OpenPort();
    }

    public void openPorts() { this.openPorts(Bukkit.getConsoleSender()); }

    public void openPorts(CommandSender sender) {
        configHelper.reload();
        configHelper.config.ports.forEach(port -> {
            Component open = AutoUPnPUtil.replace(OPEN_PORTS_OPEN, "<port>", port.toString());
            Component trying = AutoUPnPUtil.replace(OPEN_PORTS_TRY, "<port>", port.toString());
            Component success = AutoUPnPUtil.replace(OPEN_PORTS_SUCCESS, "<port>", port.toString());

            if(ports.contains(port)) {
                getComponentLogger().info(open);
                if(!(sender instanceof ConsoleCommandSender)) sender.sendMessage(open);
                return;
            }

            try {
                getComponentLogger().info(trying);
                if(!(sender instanceof ConsoleCommandSender)) sender.sendMessage(trying);

                openPort(port);
                getComponentLogger().info(success);
                if(!(sender instanceof ConsoleCommandSender)) sender.sendMessage(success);
            } catch (RuntimeException ex) {
                if(ex instanceof AutoUPnPPortDisabledException) return;
                Component failure = AutoUPnPUtil.replace(OPEN_PORTS_FAILED,
                        new ReplacementPair("<port>", port.toString()),
                        new ReplacementPair("<ex>", ex.toString()));

                getComponentLogger().trace(failure, ex);
                if(!(sender instanceof ConsoleCommandSender)) sender.sendMessage(failure);
            }
        });
    }

    @Override
    public void onDisable() {
        ports.forEach(port -> {
            UPnP.defaultGW.closePort(port.externalPort(), port.protocol() == Protocol.UDP);
            getComponentLogger().info(AutoUPnPUtil.replace(ON_DISABLE_CLOSE, "<port>", port.toString()));
        });

        ports.clear();
    }

    public void openPort(ConfigHelper.Port port) throws RuntimeException {
        if(port.disabled()) throw new AutoUPnPPortDisabledException();

        UPnP.waitInit();
        if(!UPnP.defaultGW.openPort(port.ip(), port.internalPort(), port.externalPort(), port.description(),
                port.protocol() == Protocol.UDP)) throw new RuntimeException("Failed to open port!");

        ports.add(port);
    }

}
