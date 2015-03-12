package com.archivesmc.inter.listeners;

import com.archivesmc.inter.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;

public class ConnectListener implements Listener {
    private Plugin plugin;

    public ConnectListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerConnect(PlayerLoginEvent event) {
        if (event.getResult() == PlayerLoginEvent.Result.ALLOWED) {
            this.plugin.networking.sendPlayerConnect(event.getPlayer());
        }
    }
}
