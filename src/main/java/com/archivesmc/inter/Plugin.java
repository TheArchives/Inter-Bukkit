package com.archivesmc.inter;

import com.archivesmc.inter.commands.ChatCommand;
import com.archivesmc.inter.commands.ReloadCommand;
import com.archivesmc.inter.commands.WhoCommand;
import com.archivesmc.inter.inter.Server;
import com.archivesmc.inter.listeners.ChatListener;
import com.archivesmc.inter.listeners.ConnectListener;
import com.archivesmc.inter.listeners.DisconnectListener;
import com.archivesmc.inter.listeners.KickListener;
import com.archivesmc.inter.tasks.HandlingTask;
import com.archivesmc.inter.tasks.NetworkingTask;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.command.BlockCommandSender;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.command.RemoteConsoleCommandSender;
import org.bukkit.entity.Player;
import org.bukkit.entity.minecart.CommandMinecart;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.logging.Logger;

public class Plugin extends JavaPlugin {
    private Chat chat;
    private Permission permission;

    private BukkitTask handlingBukkitTask;

    public BukkitTask networkingBukkitTask;
    public NetworkingTask networkingTask;

    public Config config;
    public Handling handling;
    public Networking networking;

    public Logger logger;

    public String name;
    public Map<String, Server> servers = new HashMap<>();
    public boolean authenticated = false;
    
    public HashSet<UUID> toggledPlayers = new HashSet<>();

    @Override
    public void onEnable() {
        // Load config, make initial connection, start tasks
        this.logger = this.getLogger();

        this.logger.info("Loading config..");
        this.config = new Config(this);

        try {
            // We only do this to ensure the address can be resolved. We don't actually need the object it returns.
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(this.config.getHost());
        } catch (UnknownHostException e) {
            this.logger.severe(String.format("Unknown host: %s", this.config.getHost()));
            this.disable();
        }

        // Create our handlers and tasks.
        this.handling = new Handling(this);
        HandlingTask handlingTask = new HandlingTask(this);

        this.networking = new Networking(this, this.config.getApiKey(), this.config.getHost(), this.config.getPort());
        this.networkingTask = new NetworkingTask(this);

        if (!this.networking.connect()) {
            // Connect! If it didn't work, we'll just disable ourselves.
            this.disable();
            return;
        }
        
        // Start up our tasks. We do this to avoid polling on the main thread, which would be rather slow.
        this.handlingBukkitTask = this.getServer().getScheduler().runTaskTimer(this, handlingTask, 0, 1);
        this.networkingBukkitTask = this.getServer().getScheduler().runTaskLaterAsynchronously(this, this.networkingTask, 10);

        // Check that bukkit was actually happy to schedule the tasks.
        if (this.handlingBukkitTask.getTaskId() < 0) {
            this.logger.severe("Unable to schedule handling task.");
            this.disable();
            return;
        } else if (this.networkingBukkitTask.getTaskId() < 0) {  // Didn't need an else but it looks cleaner
            this.logger.severe("Unable to schedule networking task.");
            this.disable();
            return;
        }
        
        // Set up Vault
        this.setupVault();
        
        // Set up our commands
        this.getCommand("interchat").setExecutor(new ChatCommand(this));
        this.getCommand("interreload").setExecutor(new ReloadCommand(this));
        this.getCommand("interwho").setExecutor(new WhoCommand(this));

        // Finally, register event handlers.
        this.getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        this.getServer().getPluginManager().registerEvents(new ConnectListener(this), this);
        this.getServer().getPluginManager().registerEvents(new DisconnectListener(this), this);
        this.getServer().getPluginManager().registerEvents(new KickListener(this), this);
    }

    @Override
    public void onDisable() {
        // Close connection, cancel any tasks

        this.logger.info("Shutting down..");

        if (this.networking != null) {
            this.networking.disconnect();
        }
        
        if (this.networkingBukkitTask != null) {
            this.networkingBukkitTask.cancel();
        }
        
        if (this.handlingBukkitTask != null) {
            this.handlingBukkitTask.cancel();
        }
    }
    
    void setupVault() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return;
        }

        RegisteredServiceProvider<Chat> rspChat = getServer().getServicesManager().getRegistration(Chat.class);
        
        if (rspChat != null) {
            this.chat = rspChat.getProvider();
        }

        RegisteredServiceProvider<Permission> rspPermission = getServer().getServicesManager().getRegistration(Permission.class);

        if (rspPermission != null) {
            this.permission = rspPermission.getProvider();
        }
    }
    
    public void addServer(String name, ArrayList<String> players) {
        if (! this.servers.containsKey(name)) {
            Server server = new Server(name);
            server.addPlayers(players);

            this.servers.put(name, server);
        }
    }
    
    public Server getServer(String name) {
        if (! this.servers.containsKey(name)) {
            this.addServer(name, new ArrayList<String>());
        }

        return this.servers.get(name);
    }
    
    public void removeServer(String name) {
        if (this.servers.containsKey(name)) {
            this.servers.remove(name);
        }
    }

    public void addVariables(CommandSender commandSender, Map<String, Object> map) {
        if (commandSender != null) {
            
            if (commandSender instanceof Player) {
                // If it's a player, we can have player-specific stuff
                
                Player player = (Player) commandSender;

                if (this.permission != null) {
                    map.put("primaryGroup", this.permission.getPrimaryGroup(player.getWorld().getName(), player));
                }

                if (this.chat != null) {
                    map.put("prefix", this.chat.getPlayerPrefix(player));
                    map.put("suffix", this.chat.getPlayerSuffix(player));
                }

                map.put("uuid", player.getUniqueId());
                map.put("world", player.getWorld().getName());

                map.put("player", player.getName());
            } else {
                // If it's not, we fill in some defaults.
                
                if (commandSender instanceof ConsoleCommandSender || commandSender instanceof RemoteConsoleCommandSender) {
                    map.put("primaryGroup", "Console");
                    map.put("player", "Console");
                } else if (commandSender instanceof BlockCommandSender || commandSender instanceof CommandMinecart) {
                    map.put("primaryGroup", "CmdBlock");
                    map.put("player", String.format("[@%s]", commandSender.getName()));
                } else {
                    map.put("player", commandSender.getName());
                }
                
                map.put("prefix", "");
                map.put("suffix", "");
                
                map.put("uuid", "N/A");
                map.put("world", this.name);
            }
            
            map.put("user", map.get("player"));
        }
    }

    public void disable() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public void sendToPlayers(String message, boolean doColours) {
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (doColours){
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            } else {
                player.sendMessage(message);
            }
        }
        
        this.getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', message));
    }
}
