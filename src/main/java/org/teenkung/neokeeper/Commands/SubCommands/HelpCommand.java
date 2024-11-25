package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

public class HelpCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>NeoKeeper Help"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>----------------"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper create <shopID> [title]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper open <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper edit <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper remove <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper reload"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper help"));
    }

}
