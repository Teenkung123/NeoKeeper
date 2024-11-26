package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.teenkung.neokeeper.NeoKeeper;

public class RemoveCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        if (args.length == 2) {
            String id = args[1];
            if (plugin.getShopManager().getAllTradeManagers().containsKey(id)) {
                if (plugin.getShopManager().deleteShop(id, true)) {
                    player.sendMessage(plugin.colorize("<green>Successfully deleted shop id: " + id));
                    player.sendMessage(plugin.colorize("<yellow>Note: if plugin still show the shop with that ID, try reload the plugin"));
                    return;
                }
                player.sendMessage(plugin.colorize("<red>Failed to delete shop id: " + id + " please check console for more information"));
            } else {
                player.sendMessage(plugin.colorize("<red>Shop id: " + id + " does not exists!"));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper remove <shopID>"));
        }
    }

}
