package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class RemoveCommand {

    public void execute(NeoKeeper plugin, Player player, String[] args) {
        if (args.length == 3) {
            String id = args[1];
            if (plugin.getShopLoader().getAllTradeManagers().containsKey(id)) {
                plugin.getShopLoader().removeShop(id);
                plugin.reload();
            } else {
                player.sendMessage(plugin.colorize("<red>Shop id: " + id + " does not exists!"));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper edit <shopID>"));
        }
    }

}
