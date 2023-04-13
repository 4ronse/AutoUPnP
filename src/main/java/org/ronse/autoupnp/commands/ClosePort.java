package org.ronse.autoupnp.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.ConfigHelper;
import org.ronse.autoupnp.PortHelper;
import org.ronse.autoupnp.Protocol;
import org.ronse.autoupnp.records.Port;
import org.ronse.autoupnp.util.AutoUPnPUtil;
import org.ronse.autoupnp.util.ReplacementPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClosePort extends AutoUPnPCommand {
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

        final Protocol  protocol        = Protocol.fromString(args[0]);
        final int       externalPort    = Integer.parseInt(args[1]);
        int res = PortHelper.closePort(protocol, externalPort);

        if(res != PortHelper.RESULT_SUCCESS) {
            sender.sendMessage(AutoUPnPUtil.replace(AutoUPnP.FAILED_TO_EXECUTE_COMMAND,
                    new ReplacementPair("<cmd>", label),
                    new ReplacementPair("<err>", PortHelper.getLastErrorMessage())));
            return;
        }

        final AtomicReference<Port> atomicPort = new AtomicReference<>(null);
        ConfigHelper.getConfig().ports.forEach(port -> {
            if(atomicPort.get() == null && port.protocol() == protocol && port.externalPort() == externalPort) atomicPort.set(port);
        });

        if(atomicPort.get() != null) {
            ConfigHelper.getConfig().ports.remove(atomicPort.get());
            ConfigHelper.getInstance().update();
        }

        sender.sendMessage(AutoUPnPUtil.replace(AutoUPnP.PORT_CLOSE_SUCCESS, "<port>", String.valueOf(externalPort)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> pos = new ArrayList<>();

        if(args.length == 1) pos.addAll(List.of("TCP", "UDP"));
        if(args.length == 2) PortHelper.allPorts().forEach(port -> pos.add(String.valueOf(port.externalPort())));

        return pos;
    }
}
