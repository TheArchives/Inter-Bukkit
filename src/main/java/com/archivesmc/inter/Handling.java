package com.archivesmc.inter;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Handling {
    private Plugin plugin;
    private ConcurrentLinkedQueue<Map<String, Object>> queue = new ConcurrentLinkedQueue<>();

    public Handling(Plugin plugin) {
        this.plugin = plugin;
    }

    public void queueUp(Map<String, Object> data) {
        // This will be called from another thread.
        this.queue.add(data);
    }

    @SuppressWarnings("unchecked")
    public void handleMessage() {
        if (this.queue.isEmpty()) {
            return;
        }

        Map<String, Object> data = this.queue.poll();

        String from = (String) data.get("from");
        String formatted;

        switch (from) {
            case "chat":
                // We don't want to translate any colours in chat.
                for (String key : data.keySet()) {
                    if (! "message".equals(key)) {
                        data.put(key, ChatColor.translateAlternateColorCodes('&', (String) data.get(key)));
                    }
                }
                
                formatted = Utils.formatString(this.plugin.config.getStringChat(), data);
                this.plugin.sendToPlayers(formatted, false);
                
                break;
            case "players":
                String type = (String) data.get("type");

                String target;
                String player;

                switch (type) {
                    case "online":
                        target = (String) data.get("target");
                        player = (String) data.get("player");
                        
                        this.plugin.getServer(target).addPlayer(player);
                        
                        formatted = Utils.formatString(this.plugin.config.getStringPlayerConnect(), data);
                        this.plugin.sendToPlayers(formatted, true);
                        
                        break;
                    case "offline":
                        target = (String) data.get("target");
                        player = (String) data.get("player");

                        this.plugin.getServer(target).removePlayer(player);

                        formatted = Utils.formatString(this.plugin.config.getStringPlayerDisconnect(), data);
                        this.plugin.sendToPlayers(formatted, true);
                        
                        break;
                    case "list":
                        target = (String) data.get("target");
                        
                        if ("all".equals(target)) {
                            Map<String, ArrayList<String>> players = (Map<String, ArrayList<String>>) data.get("players");
                            
                            for (String key : players.keySet()) {
                                this.plugin.addServer(key, players.get(key));
                            }
                            
                            this.plugin.logger.info(String.format("Got players for %s servers.", this.plugin.servers.size()));
                        }
                }
                break;
            case "auth":
                String action = (String) data.get("action");
                String server;

                switch (action) {
                    case "disconnected":
                        server = (String) data.get("name");
                        this.plugin.removeServer(server);
                        
                        formatted = Utils.formatString(this.plugin.config.getStringServerDisconnect(), data);

                        this.plugin.sendToPlayers(formatted, true);
                        break;
                    case "authenticated":
                        if (!this.plugin.authenticated) {
                            String status = (String) data.get("status");

                            if ("success".equals(status)) {
                                this.plugin.name = (String) data.get("name");
                                this.plugin.authenticated = true;

                                this.plugin.logger.info(String.format("Authenticated as '%s'", this.plugin.name));
                                this.plugin.networking.sendGetPlayers();
                                
                                for (Player onlinePlayer : this.plugin.getServer().getOnlinePlayers()) {
                                    // Send player connections. This plugin isn't meant to be loaded late, but it can happen!
                                    this.plugin.networking.sendPlayerConnect(onlinePlayer);
                                }
                            } else {
                                String error = (String) data.get("error");
                                this.plugin.logger.severe(String.format("Error authenticating: %s", error));
                                this.plugin.disable();
                            }
                            break;
                        } else {
                            server = (String) data.get("name");
                            this.plugin.addServer(server, new ArrayList<String>());
                            
                            formatted = Utils.formatString(this.plugin.config.getStringServerConnect(), data);
                            this.plugin.sendToPlayers(formatted, true);
                        }
                }
                break;
            case "core":
                if (data.containsKey("error")) {
                    this.plugin.logger.warning(String.format("Error received from server: %s", data.get("error")));
                }
            case "ping":
                this.plugin.networking.sendPong((String) data.get("timestamp"));
                break;
        }
    }
}
