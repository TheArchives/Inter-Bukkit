package com.archivesmc.inter;

import java.util.HashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Handling {
    private Plugin plugin;
    private ConcurrentLinkedQueue<HashMap<String, Object>> queue = new ConcurrentLinkedQueue<>();

    public Handling(Plugin plugin) {
        this.plugin = plugin;
    }

    public void queueUp(HashMap<String, Object> data) {
        // This will be called from another thread.
        this.queue.add(data);
    }

    public void handleMessage() {
        if (this.queue.isEmpty()) {
            return;
        }

        HashMap<String, Object> data = this.queue.poll();

        String from = (String) data.get("from");
        String formatted;

        switch (from) {
            case "chat":
                formatted = Utils.formatString(this.plugin.config.getStringChat(), data);

                this.plugin.sendToPlayers(formatted);
                break;
            case "players":
                String type = (String) data.get("type");

                switch (type) {
                    case "online":
                        formatted = Utils.formatString(this.plugin.config.getStringPlayerConnect(), data);

                        this.plugin.sendToPlayers(formatted);
                        break;
                    case "offline":
                        formatted = Utils.formatString(this.plugin.config.getStringPlayerDisconnect(), data);

                        this.plugin.sendToPlayers(formatted);
                        break;
                    case "list":
                        // TODO: Player list sent from Inter
                }
                break;
            case "auth":
                String action = (String) data.get("action");

                switch (action) {
                    case "disconnected":
                        formatted = Utils.formatString(this.plugin.config.getStringServerDisconnect(), data);

                        this.plugin.sendToPlayers(formatted);
                        break;
                    case "authenticated":
                        if (!this.plugin.authenticated) {
                            String status = (String) data.get("status");

                            if ("success".equals(status)) {
                                this.plugin.name = (String) data.get("name");
                                this.plugin.authenticated = true;

                                this.plugin.logger.info(String.format("Authenticated as '%s'", this.plugin.name));
                                this.plugin.networking.sendGetPlayers();
                            } else {
                                String error = (String) data.get("error");
                                this.plugin.logger.severe(String.format("Error authenticating: %s", error));
                                this.plugin.disable();
                            }
                            break;
                        } else {
                            formatted = Utils.formatString(this.plugin.config.getStringServerConnect(), data);

                            this.plugin.sendToPlayers(formatted);
                        }
                }
                break;
            case "core":
                break;
            case "ping":
                this.plugin.networking.sendPong((String) data.get("timestamp"));
                break;
        }
    }
}
