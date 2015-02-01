package com.archivesmc.inter.commands;

import com.archivesmc.inter.Plugin;
import com.archivesmc.inter.Utils;
import org.apache.commons.lang.StringUtils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class WhoCommand implements CommandExecutor {
    private Plugin plugin;

    public WhoCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender commandSender, Command command, String s, String[] strings) {
        Map<String, Object> args = new HashMap<>();
        
        args.put("server", this.plugin.name);
        
        commandSender.sendMessage(
                ChatColor.translateAlternateColorCodes(
                        '&', Utils.formatString(this.plugin.config.getStringListHeader(), args)
                )
        );
        
        for (String key : this.plugin.servers.keySet()) {
            ArrayList<String> players = new ArrayList<>();
            args.clear();
            
            for (String player : this.plugin.servers.get(key).getPlayers()) {
                args.put("player", player);
                
                players.add(Utils.formatString(this.plugin.config.getStringListPlayer(), args));
            }

            args.clear();
            
            String done = StringUtils.join(players, ", ");
            
            args.put("server", key);
            
            if (done.isEmpty()) {
                Map<String, Object> args2 = new HashMap<>();
                args2.put("player", "No players online.");

                args.put("players", Utils.formatString(
                        this.plugin.config.getStringListPlayer(),
                        args2
                ));
                
                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                                '&', Utils.formatString(
                                        this.plugin.config.getStringListServer(),
                                        args
                                )
                        )
                );
            } else {
                args.put("players", done);
                
                commandSender.sendMessage(
                        ChatColor.translateAlternateColorCodes(
                                '&', Utils.formatString(
                                        this.plugin.config.getStringListServer(),
                                        args
                                )
                        )
                );
            }
        }
        
        return true;
    }
}
