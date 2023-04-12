package org.ronse.autoupnp.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.ConfigHelper;
import org.ronse.autoupnp.Protocol;
import org.ronse.autoupnp.util.AutoUPnPUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class OpenPort extends AutoUPnPCommand {
    public OpenPort() {
        super("open-port", "AutoUPnP.manage", List.of(),
                new String[] { "IP", "Internal Port", "External Port", "Protocol", "Description" });
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> pos = new ArrayList<>();

        if(args.length == 1) AutoUPnP.ports.forEach(port -> pos.add(port.ip()));
        if(args.length == 4) pos.addAll(List.of("TCP", "UDP"));

        return pos;
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        Component errorComp = validateNumArguments(5, args);
        if(errorComp != null) {
            sender.sendMessage(errorComp);
            return;
        }

        final String ip             = args[0];
        final int internal          = Integer.parseInt(args[1]);
        final int external          = Integer.parseInt(args[2]);
        final Protocol protocol     = Protocol.fromString(args[3]);
        final String description    = String.join(" ", Arrays.copyOfRange(args, 4, args.length));

        ConfigHelper.Port port = new ConfigHelper.Port(ip, internal, external, protocol, description, false);
        if(AutoUPnP.configHelper.config.ports.contains(port)) {
            sender.sendMessage(AutoUPnPUtil.replace(AutoUPnP.OPEN_PORTS_OPEN, "<port>", port.toString()));
            return;
        }

        AutoUPnP.configHelper.config.ports.add(port);
        AutoUPnP.configHelper.update();
        AutoUPnP.instance.openPorts(sender);
    }
}
