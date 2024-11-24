package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class CreateCommand {

    public void execute(NeoKeeper plugin, Player player, String[] args) {
        if (args.length == 2) {
            String id = args[1];
            if (plugin.getShopLoader().getAllTradeManagers().containsKey(id)) {
                player.sendMessage(plugin.colorize("<red>Shop id: " + id + " Already Exists!"));
            } else {
                plugin.getShopLoader().addShop(id, args[1]);
                player.sendMessage(plugin.colorize("<green>Shop id: " + id + " Created!"));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper create <shopID>"));
        }
    }

}
