package org.ronse.autoupnp.commands;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.command.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.ronse.autoupnp.AutoUPnP;
import org.ronse.autoupnp.exceptions.AutoUPnPNotValidatorFunction;
import org.ronse.autoupnp.util.AutoUPnPUtil;
import org.ronse.autoupnp.util.validation.Validator;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public abstract class AutoUPnPCommand implements CommandExecutor, TabCompleter {
    public static final String DEFAULT_PERMISSION = "AutoUPnP.manage";

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

    public AutoUPnPCommand(String name) {
        this(name, DEFAULT_PERMISSION);
    }

    public AutoUPnPCommand(String name, String permission) {
        this(name, permission, List.of());
    }

    public AutoUPnPCommand(String name, List<String> aliases) {
        this(name, DEFAULT_PERMISSION, aliases);
    }

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
        Audience audience = AutoUPnP.instance.adventure().sender(sender);

        if(sender instanceof ConsoleCommandSender || permission == null || sender.hasPermission(permission)) execute(sender, audience, command, label, args);
        else audience.sendMessage(AutoUPnPUtil.replace(NO_PERMISSION, "<command-name>", command.getName()));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        return List.of();
    }

    public abstract void execute(CommandSender sender, Audience audience, Command command, String label, String[] args);

    public int numArgs() { return -1; }
    public int minArgs() { return -1; }
    public int maxArgs() { return -1; }

    protected final boolean validateArgs(CommandSender sender, String[] args) {
        Audience audience = AutoUPnP.instance.adventure().sender(sender);

        TextComponent[] validationMessages = prepareMessages(args);
        if(validationMessages.length == 0) return true;
        for(TextComponent vm : validationMessages)
            audience.sendMessage(AutoUPnP.PREFIX.append(vm.color(TextColor.color(AutoUPnP.COLOR_DANGER))));

        return false;
    }

    private TextComponent[] prepareMessages(String[] args) {
        if(numArgs() >= 0 && numArgs() != args.length) return new TextComponent[]{Component.text("Number of arguments is invalid.")};
        if(minArgs() >= 0 && minArgs() > args.length)  return new TextComponent[]{Component.text("Not enough arguments.")};
        if(maxArgs() >= 0 && maxArgs() < args.length)  return new TextComponent[]{Component.text("Too many arguments")};

        String[] res = _validateArgs(args);
        TextComponent[] comps = new TextComponent[res.length];
        for (int i = 0; i < res.length; i++) comps[i] = Component.text(res[i] + " is either invalid or missing.");

        return comps;
    }

    private String[] _validateArgs(String[] args) {
        ArrayList<String> invalidArgs = new ArrayList<>();

        Class<? extends AutoUPnPCommand> clazz = this.getClass();
        Method[] publicMethods = clazz.getMethods();
        Method[] privateMethods = clazz.getDeclaredMethods();

        Set<Method> methods = new HashSet<>();
        methods.addAll(Arrays.asList(publicMethods));
        methods.addAll(Arrays.asList(privateMethods));

        for(Method method : methods) {
            final Validator v = method.getAnnotation(Validator.class);
            if(v == null) continue;

            Class<?>[] paramTypes = method.getParameterTypes();
            if(paramTypes.length != 1 || paramTypes[0] != String.class) throw new AutoUPnPNotValidatorFunction();

            method.setAccessible(true);

            try {
                if (!((boolean) method.invoke(this, args[v.position()]))) invalidArgs.add(v.name());
            } catch (IllegalAccessException | InvocationTargetException ex) {
                AutoUPnP.instance.getComponentLogger().trace("Failed to validate", ex);
            } catch (ArrayIndexOutOfBoundsException ex) {
                if(!v.required()) continue;
                invalidArgs.add(v.name());
            }
        }

        Collections.reverse(invalidArgs);
        String[] invalidArgsArray = new String[invalidArgs.size()];
        return invalidArgs.toArray(invalidArgsArray);
    }
}
