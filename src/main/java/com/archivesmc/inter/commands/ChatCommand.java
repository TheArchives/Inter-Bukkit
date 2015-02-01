package com.archivesmc.inter.commands;

import com.archivesmc.inter.Plugin;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class ChatCommand implements CommandExecutor {
    private Plugin plugin;

    public ChatCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        if (strings.length > 0) {
            String message = StringUtils.join(strings, ' ');
            
            // Sure is much easier than how we used to do this..
            this.plugin.networking.sendChat(commandSender, message);
        } else {
            if (commandSender instanceof Player) {
                UUID id = ((Player) commandSender).getUniqueId();
                if (this.plugin.toggledPlayers.contains(id)) {
                    this.plugin.toggledPlayers.remove(id);
                    
                    commandSender.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', this.plugin.config.getStringToggleDisabled())
                    );
                } else {
                    this.plugin.toggledPlayers.add(id);

                    commandSender.sendMessage(
                            ChatColor.translateAlternateColorCodes('&', this.plugin.config.getStringToggleEnabled())
                    );
                }
            } else {
                // Only players have a "default" chat. So only players can use inter by default.
                commandSender.sendMessage("Usage: /inc <message>");
            }
        }
        return true;
    }
}
