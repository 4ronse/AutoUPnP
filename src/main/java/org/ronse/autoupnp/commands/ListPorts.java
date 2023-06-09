package org.ronse.autoupnp.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.PortHelper;
import org.ronse.autoupnp.util.AutoUPnPUtil;
import org.ronse.autoupnp.util.ReplacementPair;

public class ListPorts extends AutoUPnPCommand {
    public ListPorts() {
        super("list-ports");
    }

    @Override
    public void execute(CommandSender sender, Audience audience, Command command, String label, String[] args) {
        if(!validateArgs(sender, args)) return;

        PortHelper.allPorts().forEach(port -> {
            Component comp = AutoUPnP.PREFIX.append(
                    Component.text("<ip>:<int> -> <ext> [<protocol>]").color(TextColor.color(AutoUPnP.COLOR_INFO))
            );

            audience.sendMessage(AutoUPnPUtil.replace(comp,
                    new ReplacementPair("<ip>", port.ip()),
                    new ReplacementPair("<int>", String.valueOf(port.internalPort())),
                    new ReplacementPair("<ext>", String.valueOf(port.externalPort())),
                    new ReplacementPair("<protocol>", port.protocol().toString())));
        });
    }

    @Override
    public int numArgs() {
        return 0;
    }
}
