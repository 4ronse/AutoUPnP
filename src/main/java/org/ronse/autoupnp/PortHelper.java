package org.ronse.autoupnp;

import com.dosse.upnp.Gateway;
import com.dosse.upnp.UPnP;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextColor;
import org.jetbrains.annotations.NotNull;
import org.ronse.autoupnp.records.Port;
import org.ronse.autoupnp.util.AutoUPnPUtil;
import org.ronse.autoupnp.util.ReplacementPair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * PortHelper contains all things related to port forwarding and closing
 */
public class PortHelper {
    public static final String ERROR_NO_GATEWAY         = "Gateway not found.";
    public static final String ERROR_PORT_NOT_OPEN      = "<port> is not open.";
    public static final String ERROR_PORT_IS_OPEN       = "<port> is already open.";
    public static final String ERROR_PORT_IS_DISABLED   = "<port> is disabled.";

    public static final int RESULT_ERROR_GENERIC        = -1;
    public static final int RESULT_SUCCESS              = 0;
    public static final int RESULT_NO_GATEWAY           = 1;
    public static final int RESULT_PORT_NOT_OPEN        = 2;
    public static final int RESULT_PORT_ALREADY_OPEN    = 3;
    public static final int RESULT_PORT_DISABLED        = 4;

    private static final List<Port> openPorts = new ArrayList<>();
    private static Gateway gateway = null;
    private static String lastErrorMessage = null;

    /**
     * Initialize {@link UPnP}
     * Calls {@link UPnP#waitInit()} and sets {@link #gateway}
     */
    public static void initialize() {
        if(gateway != null) return;

        AutoUPnP.instance.getComponentLogger().info(AutoUPnP.PREFIX.append(Component.text("Initializing...").color(TextColor.color(AutoUPnP.COLOR_INFO))));
        UPnP.waitInit();
        gateway = UPnP.defaultGW;
        AutoUPnP.instance.getComponentLogger().info(AutoUPnP.PREFIX.append(Component.text("Ready!").color(TextColor.color(AutoUPnP.COLOR_SUCCESS))));
    }

    /**
     * Opens all given ports
     * @param ports Array of {@link Port} to open
     * @return Result
     */
    public static int openPorts(final Port... ports) {
        for(Port port : ports) {
            final int res = openPort(port);
            if(res != RESULT_SUCCESS) return res;
        }

        return RESULT_SUCCESS;
    }

    /**
     * Opens all given ports
     * @param ports {@link List<Port>} of {@link Port} to open
     * @return Result
     */
    public static int openPorts(@NotNull final List<Port> ports) {
        Port[] pArr = new Port[ports.size()];
        ports.toArray(pArr);
        return openPorts(pArr);
    }

    /**
     * Tries to open a port
     * @param port {@link Port} to open
     * @return Result
     */
    public static int openPort(@NotNull final Port port) {
        initialize();

        if(port.disabled()) return setErrorMessage(RESULT_PORT_DISABLED, ERROR_PORT_IS_DISABLED, new ReplacementPair("<port>", String.valueOf(port.externalPort())));
        if(isPortOpen(port)) return setErrorMessage(RESULT_PORT_ALREADY_OPEN, ERROR_PORT_IS_OPEN, new ReplacementPair("<port>", String.valueOf(port.externalPort())));
        gateway.openPort(port.ip(), port.internalPort(), port.externalPort(), port.description(), port.protocol().isUDP());
        openPorts.add(port);
        openPorts.sort(Port::compareTo);

        AutoUPnP.instance.getComponentLogger().info(AutoUPnPUtil.replace(AutoUPnP.PORT_OPEN_SUCCESS, "<port>", port.toString()));
        return RESULT_SUCCESS;
    }

    /**
     * Closes all given ports
     * @param ports Array of {@link Port} to close
     * @return Result
     */
    public static int closePorts(final Port... ports) {
        for(Port port : ports) {
            final int res = closePort(port);
            if(res != RESULT_SUCCESS) return res;
        }

        return RESULT_SUCCESS;
    }

    /**
     * Closes all given ports
     * @param ports {@link List<Port>} of {@link Port} to close
     * @return Result
     */
    public static int closePorts(@NotNull final List<Port> ports) {
        Port[] pArr = new Port[ports.size()];
        ports.toArray(pArr);
        return closePorts(pArr);
    }

    /**
     * Tries to close port
     * @param port {@link Port} to close
     * @return Result
     */
    public static int closePort(final Port port) {
        if(port == null) return RESULT_ERROR_GENERIC;
        if(gateway == null) return setErrorMessage(RESULT_NO_GATEWAY, ERROR_NO_GATEWAY);
        if(!isPortOpen(port)) return setErrorMessage(RESULT_PORT_NOT_OPEN, ERROR_PORT_NOT_OPEN, new ReplacementPair("<port>", String.valueOf(port.externalPort())));

        gateway.closePort(port.externalPort(), port.protocol().isUDP());
        openPorts.remove(port);

        AutoUPnP.instance.getComponentLogger().info(AutoUPnPUtil.replace(AutoUPnP.PORT_CLOSE_SUCCESS, "<port>", port.toString()));
        return RESULT_SUCCESS;
    }

    /**
     * Searches for port. If the port is found then it'll call
     * {@link #closePort(Port)} to close it.
     * @param prot  {@link Protocol#TCP} or {@link Protocol#UDP}
     * @param ext   External port number
     * @return Result
     */
    public static int closePort(@NotNull final Protocol prot, final int ext) {
        int res = closePort(getOpenPort(prot, ext));
        if(res == RESULT_ERROR_GENERIC)
            res = setErrorMessage(RESULT_PORT_NOT_OPEN, ERROR_PORT_NOT_OPEN, new ReplacementPair("<port>", String.valueOf(ext)));

        return res;
    }

    /**
     * Tries to get port from list of open ports ({@link #openPorts})
     * {@link #closePort(Port)} to close it.
     * @param prot  {@link Protocol#TCP} or {@link Protocol#UDP}
     * @param ext   External port number
     * @return {@link Port} or {@code null}
     */
    public static Port getOpenPort(@NotNull final Protocol prot, final int ext) {
        final AtomicReference<Port> atomicPort = new AtomicReference<>(null);

        openPorts.forEach(port -> {
            if(atomicPort.get() != null) return;
            if(port.protocol() == prot && port.externalPort() == ext) atomicPort.set(port);
        });

        return atomicPort.get();
    }

    /**
     * Checks whether port is open or not
     * @param port {@link Port}
     * @return Is port open
     */
    public static boolean isPortOpen(final Port port) {
        return port != null && openPorts.contains(port);
    }

    public static String getLastErrorMessage() {
        return lastErrorMessage;
    }

    private static int setErrorMessage(final int err, String msg, ReplacementPair... pairs) {
        for(ReplacementPair pair : pairs) msg = msg.replace(pair.left(), pair.right());
        lastErrorMessage = msg;

        return err;
    }

    public static List<Port> allPorts() {
        return List.copyOf(openPorts);
    }
}
