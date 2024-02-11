package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class EditCommand {

    public void execute(NeoKeeper plugin, Player player, String[] args) {
        if (args.length == 2) {
            String id = args[1];
            if (plugin.getShopLoader().getAllTradeManagers().containsKey(id)) {
                plugin.getShopLoader().getTradeManager(id).buildEditGUI(player);
            } else {
                player.sendMessage(plugin.colorize("<red>Could not find shop with id: " + id));
            }
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper edit <shopID>"));
        }
    }

}
