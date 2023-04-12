package org.ronse.autoupnp.commands;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.util.AutoUPnPUtil;

import java.util.List;

public abstract class AutoUPnPCommand implements CommandExecutor, TabCompleter {
    public static final TextComponent NO_PERMISSION;
    public static final TextComponent COMMAND_LOADED;
    public static final TextComponent MISSING_ARGUMENTS;

    static {
        NO_PERMISSION = AutoUPnP.PREFIX.append(Component.text("Permission for command").color(TextColor.color(AutoUPnP.COLOR_DANGER))
                .append(Component.text(" <command-name> ").style(Style.style(TextColor.color(AutoUPnP.COLOR_DANGER), TextDecoration.ITALIC)))
                .append(Component.text("is denied.").color(TextColor.color(AutoUPnP.COLOR_DANGER))));

        COMMAND_LOADED = AutoUPnP.PREFIX.append(Component.text("Command").color(TextColor.color(AutoUPnP.COLOR_SUCCESS))
                .append(Component.text(" <command-name> ").style(Style.style(TextColor.color(AutoUPnP.COLOR_SUCCESS), TextDecoration.ITALIC)))
                .append(Component.text("loaded.").color(TextColor.color(AutoUPnP.COLOR_SUCCESS))));

        MISSING_ARGUMENTS = AutoUPnP.PREFIX.append(Component.text("<arguments>").style(Style.style(TextColor.color(AutoUPnP.COLOR_DANGER), TextDecoration.ITALIC))
                .append(Component.text(" argument(s) missing.").color(TextColor.color(AutoUPnP.COLOR_DANGER))));
    }

    protected String permission;
    protected final String[] argNames;

    public AutoUPnPCommand(String name, String perm, List<String> aliases) {
        this(name, perm, aliases, new String[]{});
    }

    public AutoUPnPCommand(String name, String perm, List<String> aliases, final String[] argNames) {
        PluginCommand command = AutoUPnP.instance.getCommand(name);
        assert command != null;

        command.setExecutor(this);
        command.setTabCompleter(this);
        command.setAliases(aliases);

        permission = perm;
        this.argNames = argNames;

        AutoUPnP.instance.getComponentLogger().info(AutoUPnPUtil.replace(COMMAND_LOADED, "<command-name>", name));
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(sender instanceof ConsoleCommandSender || permission == null || sender.hasPermission(permission)) execute(sender, command, label, args);
        else sender.sendMessage(AutoUPnPUtil.replace(NO_PERMISSION, "<command-name>", command.getName()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    public abstract void execute(CommandSender sender, Command command, String label, String[] args);
}
