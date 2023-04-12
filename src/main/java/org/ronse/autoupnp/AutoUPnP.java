package org.ronse.autoupnp;

import com.dosse.upnp.UPnP;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;

public final class AutoUPnP extends JavaPlugin {
    public static AutoUPnP instance;
    public final ArrayList<Pair<Integer, Boolean>> ports;

    // Static messages, don't see why they should be inlined
    static final TextComponent SEARCHING;
    static final TextComponent DONE_SEARCH;

    static {
        SEARCHING = Component.text("Searching for UPnP enabled device").color(TextColor.color(0x00A680));
        DONE_SEARCH = Component.text("UPnP Gateway found!").color(TextColor.color(0x00A680));
    }

    public AutoUPnP() {
        instance = this;
        ports = new ArrayList<>();
    }

    @Override
    public void onEnable() {
        getComponentLogger().info(SEARCHING);
        UPnP.waitInit();
        getComponentLogger().info(DONE_SEARCH);


        ConfigHelper helper = new ConfigHelper();
        helper.config.ports.forEach(port -> {
            UPnP.defaultGW.openPort(port.ip(), port.internalPort(), port.externalPort(), port.description(),
                    port.protocol() == Protocol.UDP
            );

            getComponentLogger().info(Component.text("%s:%d forwarded".formatted(port.ip(), port.internalPort()))
                    .color(TextColor.color(0x008030)));

            ports.add(new Pair<>() {
                @Override
                public Integer left() {
                    return port.externalPort();
                }

                @Override
                public Boolean right() {
                    return port.protocol() == Protocol.UDP;
                }

            });

        }
        );
    }

    @Override
    public void onDisable() {
        this.ports.forEach(pair -> {
            UPnP.defaultGW.closePort(pair.left(), pair.right());
            getComponentLogger().info(Component.text("%d disabled!".formatted(pair.left()))
                    .color(TextColor.color(0xA00000)));
        });
    }
}
