package com.archivesmc.inter;

import org.bukkit.configuration.file.FileConfiguration;

public class Config {
    private FileConfiguration configuration;
    private Plugin plugin;

    public Config(Plugin plugin) {
        this.plugin = plugin;
        // Save the default configuration from the jar, if we don't have one already
        this.plugin.saveDefaultConfig();
        
        // Load up, reloading if needed
        this.reload();
    }
    
    public void reload() {
        this.plugin.reloadConfig();
        this.configuration = this.plugin.getConfig();
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
    
    public String getStringToggleEnabled() {
        return this.configuration.getString("formatting.toggleEnabled");
    }

    public String getStringToggleDisabled() {
        return this.configuration.getString("formatting.toggleDisabled");
    }

    public String getStringListHeader() {
        return this.configuration.getString("formatting.listHeader");
    }
    
    public String getStringListServer() {
        return this.configuration.getString("formatting.listServer");
    }
    
    public String getStringListPlayer() {
        return this.configuration.getString("formatting.listPlayer");
    }
    
    public String getStringConfigReloaded() {
        return this.configuration.getString("formatting.configReloaded");
    }

    public String getStringNotAMessage() {
        return this.configuration.getString(
                "formatting.notAMessage",
                "&4\u00bb &6\"&d{message}&6\" &cis not a meaningful message. Please put some effort into your messages."
        );
    }
}
