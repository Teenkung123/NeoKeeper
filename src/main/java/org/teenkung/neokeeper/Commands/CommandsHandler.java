package org.teenkung.neokeeper.Commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.Commands.SubCommands.*;
import org.teenkung.neokeeper.NeoKeeper;

public class CommandsHandler implements CommandExecutor {

    private final NeoKeeper plugin;
    private final OpenCommand openCommand;
    private final EditCommand editCommand;
    private final CreateCommand createCommand;
    private final RemoveCommand removeCommand;
    private final ReloadCommand reloadCommand;

    public CommandsHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.openCommand = new OpenCommand();
        this.editCommand = new EditCommand();
        this.createCommand = new CreateCommand();
        this.removeCommand = new RemoveCommand();
        this.reloadCommand = new ReloadCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender instanceof Player player) {
            if (args.length > 0) {
                String main = args[0].toLowerCase();
                switch (main) {
                    case "open" -> openCommand.execute(plugin, player, args);
                    case "edit" -> editCommand.execute(plugin, player, args);
                    case "create" -> createCommand.execute(plugin, player, args);
                    case "remove" -> removeCommand.execute(plugin, player, args);
                    case "reload" -> reloadCommand.execute(plugin, player, args);
                    default -> {
                    }
                }
            }
        } else {
            sender.sendMessage(plugin.colorize("<red>This command can only executed by a player!"));
        }
        return false;
    }
}
