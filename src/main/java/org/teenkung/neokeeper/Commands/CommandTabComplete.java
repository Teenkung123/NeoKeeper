package org.teenkung.neokeeper.Commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.ArrayList;
import java.util.List;

public class CommandTabComplete implements TabCompleter {

    private final NeoKeeper plugin;
    public CommandTabComplete(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        ArrayList<String> result = new ArrayList<>();
        if (args.length == 1) {
            result.add("edit");
            result.add("open");
            result.add("help");
            result.add("reload");
            result.add("create");
            result.add("remove");
        } else if (args.length == 2) {
            String arg = args[0].toLowerCase();
            if (arg.equalsIgnoreCase("edit") || arg.equalsIgnoreCase("open")) {
                result.addAll(plugin.getShopLoader().getAllTradeManagers().keySet());
            }
        }

        return result;
    }
}
