package com.archivesmc.inter.listeners;

import com.archivesmc.inter.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class DisconnectListener implements Listener {
    private Plugin plugin;

    public DisconnectListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.plugin.toggledPlayers.remove(event.getPlayer().getUniqueId());
        this.plugin.networking.sendplayerDisconnect(event.getPlayer());
    }
}
