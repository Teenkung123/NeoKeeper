package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class RemoveCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        if (args.length == 2) {
            String id = args[1];
            if (plugin.getShopLoader().getAllTradeManagers().containsKey(id)) {
                plugin.getShopLoader().deleteShop(id, true);
                plugin.reload();
            } else {
                player.sendMessage(plugin.colorize("<red>Shop id: " + id + " does not exists!"));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper remove <shopID>"));
        }
    }

}
