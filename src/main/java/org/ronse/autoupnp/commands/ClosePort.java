package org.ronse.autoupnp.commands;

import net.kyori.adventure.audience.Audience;
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
import org.ronse.autoupnp.util.validation.Validator;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ClosePort extends AutoUPnPCommand {
    public ClosePort() {
        super("close-port", DEFAULT_PERMISSION, List.of(), new String[] { "Protocol", "External Port" });
    }

    @Override
    public void execute(CommandSender sender, Audience audience, Command command, String label, String[] args) {
        if(!validateArgs(sender, args)) return;

        final Protocol  protocol        = Protocol.fromString(args[0]);
        final int       externalPort    = Integer.parseInt(args[1]);
        int res = PortHelper.closePort(protocol, externalPort);

        if(res != PortHelper.RESULT_SUCCESS) {
            audience.sendMessage(AutoUPnPUtil.replace(AutoUPnP.FAILED_TO_EXECUTE_COMMAND,
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

        audience.sendMessage(AutoUPnPUtil.replace(AutoUPnP.PORT_CLOSE_SUCCESS, "<port>", String.valueOf(externalPort)));
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        List<String> pos = new ArrayList<>();

        if(args.length == 1) pos.addAll(List.of("TCP", "UDP"));
        if(args.length == 2) PortHelper.allPorts().forEach(port -> pos.add(String.valueOf(port.externalPort())));

        return pos;
    }

    @Override
    public int numArgs() {
        return 2;
    }

    @Validator(name = "Protocol", position = 0)
    boolean validateProtocol(String protocol) {
        return protocol.equalsIgnoreCase("TCP") || protocol.equalsIgnoreCase("UDP");
    }

    @Validator(name = "External Port", position = 1)
    boolean validateExternalPort(String arg) {
        try {
            int port = Integer.parseInt(arg);
            return port > 0 && port < 65536;
        } catch (Exception ex) {
            return false;
        }
    }
}
