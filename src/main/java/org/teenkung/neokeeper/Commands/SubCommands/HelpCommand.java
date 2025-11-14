package org.teenkung.neokeeper.Commands.SubCommands;

import org.bukkit.command.CommandSender;
import org.teenkung.neokeeper.NeoKeeper;

public class HelpCommand {

    public void execute(NeoKeeper plugin, CommandSender player, String[] args) {
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>NeoKeeper Help"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>----------------"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper create <shopID> [title]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper open <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper edit <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper remove <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper bindnpc <shopID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper npc [title]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station npc [title]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station bindnpc <stationID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station create <stationID> [title]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station open <stationID> [player]"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station edit <stationID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper station remove <stationID>"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper reload"));
        player.sendMessage(plugin.colorize(plugin.getPrefix() + "    <green>/neokeeper help"));
    }

}
