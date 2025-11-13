package org.teenkung.neokeeper.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
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
    private final HelpCommand helpCommand;
    private final BindNPCCommand bindNPCCommand;
    private final NpcCommand npcCommand;

    public CommandsHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.openCommand = new OpenCommand();
        this.editCommand = new EditCommand();
        this.createCommand = new CreateCommand();
        this.removeCommand = new RemoveCommand();
        this.reloadCommand = new ReloadCommand();
        this.helpCommand = new HelpCommand();
        this.bindNPCCommand = new BindNPCCommand();
        this.npcCommand = new NpcCommand();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (sender.hasPermission("neokeeper.admin")) {
            if (args.length > 0) {
                String main = args[0].toLowerCase();
                switch (main) {
                    case "open" -> openCommand.execute(plugin, sender, args);
                    case "edit" -> editCommand.execute(plugin, sender, args);
                    case "create" -> createCommand.execute(plugin, sender, args);
                    case "remove" -> removeCommand.execute(plugin, sender, args);
                    case "bindnpc" -> bindNPCCommand.execute(plugin, sender, args);
                    case "reload" -> reloadCommand.execute(plugin, sender, args);
                    case "npc" -> npcCommand.execute(plugin, sender, args);
                    default -> {
                        helpCommand.execute(plugin, sender, args);
                    }
                }
            }
        }
        return false;
    }
}
