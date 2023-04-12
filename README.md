# AutoUPnP - Any Bukkit fork plugin

AutoUPnP is a plugin that will let you open UPnP ports without tinkering with your router. <br>
<b>(Note: UPnP needs to be on)</b>

## Installation and Configuration

Simple download the jar plugin and put it into the `plugins/` folder. <br><br>
After reloading the server for the first,
a new folder will be made `plugins/AutoUPnP/` and in it is a `ports.yml` file. <br>

It should look like this:
```yaml
ports:
- ip: <InternalIP>
  internalPort: <InternalPort>
  externalPort: <ExternalPort>
  protocol: <TCP/UDP>
  description: <PortDescription>
```

By default it should be
```yaml
ports:
- ip: <YourInternalIP>
  internalPort: 25565
  externalPort: 25565
  protocol: TCP
  description: Minecraft Server
```

## Adding port

If you wish to add a port you simply append the list. <br><br>
Example:
```yaml
ports:
- ip: 192.168.1.100
  internalPort: 25565
  externalPort: 25565
  protocol: TCP
  description: Minecraft Server
- ip: 192.168.1.100
  internalPort: 8080
  externalPort: 80
  protocol: TCP
  description: Map Plugin
```