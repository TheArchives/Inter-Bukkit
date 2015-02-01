package com.archivesmc.inter.listeners;

import com.archivesmc.inter.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    private Plugin plugin;

    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAsyncChat(AsyncPlayerChatEvent event) {
        if (this.plugin.toggledPlayers.contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
            
            this.plugin.networking.sendChat(event.getPlayer(), event.getMessage());
        }
    }
}
