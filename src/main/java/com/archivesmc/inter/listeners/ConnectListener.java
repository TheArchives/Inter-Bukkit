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

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerConnect(PlayerLoginEvent event) {
        this.plugin.networking.sendPlayerConnect(event.getPlayer());
    }
}
