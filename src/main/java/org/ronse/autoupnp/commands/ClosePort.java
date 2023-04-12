package org.ronse.autoupnp.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.ConfigHelper;
import org.ronse.autoupnp.Protocol;
import org.ronse.autoupnp.util.AutoUPnPUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClosePort extends AutoUPnPCommand {
    public static final TextComponent PORT_CLOSED;
    public static final TextComponent PORT_CLOSED_NOTIFICATION;
    public static final TextComponent PORT_NO_EXIST;

    static {
        PORT_CLOSED                     = AutoUPnP.PREFIX.append(Component.text("<port> closed.").color(TextColor.color(AutoUPnP.COLOR_WARN)));
        PORT_CLOSED_NOTIFICATION        = AutoUPnP.PREFIX.append(Component.text("Note that you can't reopen the port without reloading the plugin").color(TextColor.color(AutoUPnP.COLOR_WARN)));
        PORT_NO_EXIST                   = AutoUPnP.PREFIX.append(Component.text("<port> isn't open").color(TextColor.color(AutoUPnP.COLOR_WARN)));
    }

    public ClosePort() {
        super("close-port", DEFAULT_PERMISSION, List.of(), new String[] { "Protocol", "External Port" });
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        Component errorComp = validateNumArguments(2, args);
        if(errorComp != null) {
            sender.sendMessage(errorComp);
            return;
        }

        final Protocol protocol = Protocol.fromString(args[0]);
        final int externalPort = Integer.parseInt(args[1]);
        final boolean removePort = args.length >= 3 && args[2].equalsIgnoreCase("true");

        ConfigHelper.Port port = getPort(externalPort, protocol);

        if(port == null) {
            sender.sendMessage(AutoUPnPUtil.replace(PORT_NO_EXIST, "<port>", String.valueOf(externalPort)));
            return;
        }

        AutoUPnP.instance.closePort(port);
        if(removePort) {
            AutoUPnP.configHelper.config.ports.remove(port);
            AutoUPnP.configHelper.update();
        }

        sender.sendMessage(AutoUPnPUtil.replace(PORT_CLOSED, "<port>", port.toString()));
        sender.sendMessage(PORT_CLOSED_NOTIFICATION);
    }

    private ConfigHelper.Port getPort(int ext, Protocol proto) {
        final AtomicReference<ConfigHelper.Port> atomicPort = new AtomicReference<>(null);
        AutoUPnP.ports.forEach(p -> { if(p.protocol() == proto && p.externalPort() == ext) atomicPort.set(p); });
        return atomicPort.get();
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> pos = new ArrayList<>();

        if(args.length == 1) pos.addAll(List.of("TCP", "UDP"));
        if(args.length == 2) AutoUPnP.ports.forEach(port -> pos.add(String.valueOf(port.externalPort())));
        if(args.length == 3) pos.addAll(List.of("True", "False"));

        return pos;
    }
}
