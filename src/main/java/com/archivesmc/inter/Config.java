package com.archivesmc.inter;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private FileConfiguration configuration;

    public Config(Plugin plugin) {
        // Save the default configuration from the jar, if we don't have one already
        plugin.saveDefaultConfig();

        // Load up the config
        this.configuration = plugin.getConfig();
    }

    public String getHost() {
        return this.configuration.getString("host");
    }

    public Integer getPort() {
        return this.configuration.getInt("port");
    }

    public String getApiKey() {
        return this.configuration.getString("apikey");
    }

    // Formatting strings

    public String getStringChat() {
        return this.configuration.getString("formatting.chat");
    }

    public String getStringPlayerConnect() {
        return this.configuration.getString("formatting.playerConnect");
    }

    public String getStringPlayerDisconnect() {
        return this.configuration.getString("formatting.playerDisconnect");
    }

    public String getStringServerConnect() {
        return this.configuration.getString("formatting.serverConnect");
    }

    public String getStringServerDisconnect() {
        return this.configuration.getString("formatting.serverDisconnect");
    }
}
