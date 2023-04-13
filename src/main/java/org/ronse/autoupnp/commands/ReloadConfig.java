package org.ronse.autoupnp.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.ConfigHelper;

public class ReloadConfig extends AutoUPnPCommand {
    public ReloadConfig() {
        super("reload-ports");
    }

    @Override
    public void execute(CommandSender sender, Command command, String label, String[] args) {
        ConfigHelper.getInstance().reload();
        AutoUPnP.instance.closeAllPorts();
        AutoUPnP.instance.openAllPorts();

        sender.sendMessage(ConfigHelper.CONFIG_RELOADED);
    }

    @Override
    public int numArgs() {
        return 0;
    }
}
