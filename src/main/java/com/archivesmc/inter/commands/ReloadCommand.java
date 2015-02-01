package com.archivesmc.inter.commands;

import com.archivesmc.inter.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {
    private Plugin plugin;

    public ReloadCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        this.plugin.config.reload();

        commandSender.sendMessage(
                ChatColor.translateAlternateColorCodes('&', this.plugin.config.getStringConfigReloaded())
        );
        
        return true;
    }
}
