package org.ronse.autoupnp.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.apache.commons.lang3.ArrayUtils;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.ConfigHelper;
import org.ronse.autoupnp.Protocol;
import org.ronse.autoupnp.util.AutoUPnPUtil;

import java.util.ArrayList;
import java.util.List;

public class OpenPort implements CommandExecutor, TabCompleter {
    public OpenPort() {
        PluginCommand command = AutoUPnP.instance.getCommand("open-port");
        assert command != null;
        command.setExecutor(this);
        command.setAliases(List.of("add-port"));
        command.setTabCompleter(this);

        AutoUPnP.instance.getComponentLogger().info(Component.text("open-port Loaded!")
                .color(TextColor.color(AutoUPnP.COLOR_INFO)));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof ConsoleCommandSender) && !sender.hasPermission("AutoUPnP.manage")) {
            sender.sendMessage(Component.text("Goofyy ahh unprivileged nihha get the fuck outa heee").color(TextColor.color(AutoUPnP.COLOR_DANGER)));
            return true;
        }

        if(args.length < 5) {
            sender.sendMessage(Component.text("Something is missing").color(TextColor.color(AutoUPnP.COLOR_DANGER)));
            return true;
        }

        final String ip             = args[0];
        final int internal          = Integer.parseInt(args[1]);
        final int external          = Integer.parseInt(args[2]);
        final Protocol protocol     = Protocol.fromString(args[3]);
        final String description    = String.join(" ", ArrayUtils.subarray(args, 3, args.length - 1));

        ConfigHelper.Port port = new ConfigHelper.Port(ip, internal, external, protocol, description, false);
        if(AutoUPnP.configHelper.config.ports.contains(port)) {
            sender.sendMessage(AutoUPnPUtil.replace(AutoUPnP.OPEN_PORTS_OPEN, "<port>", port.toString()));
            return true;
        }

        AutoUPnP.configHelper.config.ports.add(port);
        AutoUPnP.configHelper.update();
        AutoUPnP.instance.openPorts(sender);

        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> pos = new ArrayList<>();

        if(args.length == 1) AutoUPnP.ports.forEach(port -> pos.add(port.ip()));
        if(args.length == 4) pos.addAll(List.of("TCP", "UDP"));

        return pos;
    }
}
