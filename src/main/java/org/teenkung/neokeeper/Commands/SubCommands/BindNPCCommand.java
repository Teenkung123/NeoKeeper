package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.NeoKeeper;

public class BindNPCCommand {
    public void execute(NeoKeeper plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper bindNPC <shopID>"));
            return;
        }

        String shopID = args[1];
        Bukkit.dispatchCommand(sender, "npc cmd add -o neokeeper open " + shopID);
        Bukkit.dispatchCommand(sender, "npc cmd add -s -p neokeeper edit " + shopID);
        sender.sendMessage(plugin.colorize("<green>Bound Selected NPC to shop: " + shopID));
    }
}
