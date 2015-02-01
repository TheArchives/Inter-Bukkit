package com.archivesmc.inter;

import com.archivesmc.inter.inter.Server;
import net.milkbowl.vault.chat.Chat;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.logging.Logger;

public class Plugin extends JavaPlugin {
    private Chat chat;
    private Permission permission;

    public Config config;
    public Handling handling;
    public Networking networking;

    public Logger logger;

    public String name;
    public HashMap<String, Server> servers;
    public boolean authenticated = false;

    @Override
    public void onEnable() {
        // Load config, make initial connection, start tasks
        this.logger = this.getLogger();

        this.logger.info("Loading config..");
        this.config = new Config(this);

        try {
            //noinspection ResultOfMethodCallIgnored
            InetAddress.getByName(this.config.getHost());
        } catch (UnknownHostException e) {
            this.logger.severe(String.format("Unknown host: %s", this.config.getHost()));
            this.disable();
        }

        this.networking = new Networking(this, this.config.getApiKey(), this.config.getHost(), this.config.getPort());
        this.handling = new Handling(this);

        if (!this.networking.connect()) {
            this.disable();
        }
    }

    @Override
    public void onDisable() {
        // Close connection, cancel any tasks

        this.logger.info("Shutting down..");

        if (this.networking != null) {
            this.networking.disconnect();
        }
    }

    public void addVariables(HashMap<String, Object> map) {
        this.addVariables(null, map);
    }

    public void addVariables(Player player, HashMap<String, Object> map) {
        if (player != null) {
            if (this.permission != null) {
                map.put("primaryGroup", this.permission.getPrimaryGroup(player.getWorld().getName(), player));
            }

            if (this.chat != null) {
                map.put("prefix", this.chat.getPlayerPrefix(player));
                map.put("suffix", this.chat.getPlayerSuffix(player));
            }

            map.put("player", player.getName());
            map.put("uuid", player.getUniqueId());
            map.put("world", player.getWorld().getName());
        }
    }

    public void disable() {
        getServer().getPluginManager().disablePlugin(this);
    }

    public void sendToPlayers(String message) {
        for (Player player : this.getServer().getOnlinePlayers()) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
        }
    }

    public void sendToPlayers(String message, String permission) {
        for (Player player : this.getServer().getOnlinePlayers()) {
            if (player.hasPermission(permission)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', message));
            }
        }
    }
}
