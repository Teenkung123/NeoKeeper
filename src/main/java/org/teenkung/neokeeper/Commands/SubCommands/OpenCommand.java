package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class OpenCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        if (!(player instanceof Player)) {
            player.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }
        String id = args[1];
        if (plugin.getShopLoader().getAllTradeManagers().containsKey(id)) {
            if (args.length == 2) {
                plugin.getShopLoader().getTradeManager(id).buildTradeGUI((Player) player);
            } else {
                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(plugin.colorize("<red>Could not find online player named: " + args[2]));
                } else {
                    plugin.getShopLoader().getTradeManager(id).buildTradeGUI(target);
                }
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Could not find shop with id: " + id));
        }
    }

}
