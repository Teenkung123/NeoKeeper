package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class EditCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        if (!(player instanceof Player)) {
            player.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }
        if (args.length == 2) {
            String id = args[1];
            if (plugin.getShopManager().getAllTradeManagers().containsKey(id)) {
                plugin.getShopManager().getTradeManager(id).getEditGUI().openEditGUI((Player) player);
            } else {
                player.sendMessage(plugin.colorize("<red>Could not find shop with id: " + id));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper edit <shopID>"));
        }
    }

}
