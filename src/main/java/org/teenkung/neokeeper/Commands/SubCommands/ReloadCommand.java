package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class ReloadCommand {

    public void execute(NeoKeeper plugin, Player player, String[] args) {
        long time = System.currentTimeMillis();
        player.sendMessage(plugin.colorize("<green>Reloading NeoKeeper..."));
        plugin.reload();
        player.sendMessage(plugin.colorize("<green>NeoKeeper reloaded in " + (System.currentTimeMillis() - time) + "ms"));
    }

}
