package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.teenkung.neokeeper.NeoKeeper;

public class CreateCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        if (args.length >= 2) {
            String id = args[1];
            StringBuilder title = new StringBuilder();
            for (int i = 2; i < args.length; i++) {
                title.append(args[i]).append(" ");
            }
            if (plugin.getShopManager().getAllTradeManagers().containsKey(id)) {
                player.sendMessage(plugin.colorize("<red>Shop id: " + id + " already exists!"));
                return;
            }
            if (title.isEmpty()) title.append(plugin.getConfig().getString("GUI.DefaultTitle", "Default Title"));
            plugin.getShopManager().addShop(id, title.toString());
            player.sendMessage(plugin.colorize("<green>Successfully created the shop id " + id + "!"));
        } else {
            player.sendMessage(plugin.colorize("<red>Invalid Arguments! usage: /neokeeper create <shopID> [title]"));
        }
    }

}
