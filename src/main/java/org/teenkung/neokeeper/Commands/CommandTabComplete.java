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
            result.add("bindnpc");
            result.add("npc");
            result.add("station");
        } else if (args.length == 2) {
            String arg = args[0].toLowerCase();
            if (arg.equalsIgnoreCase("edit") || arg.equalsIgnoreCase("open") || arg.equalsIgnoreCase("remove") || arg.equalsIgnoreCase("bindnpc")) {
                result.addAll(plugin.getShopManager().getVisibleShopIds());
            }
            if (arg.equalsIgnoreCase("create")) {
                result.add("<shopID>");
            }
            if (arg.equalsIgnoreCase("npc")) {
                result.add("[title]");
            }
            if (arg.equalsIgnoreCase("station")) {
                result.add("open");
                result.add("edit");
                result.add("create");
                result.add("remove");
                result.add("npc");
                result.add("bindnpc");
            }
        } else if (args.length == 3) {
            String arg = args[0].toLowerCase();
            if (arg.equalsIgnoreCase("create")) {
                result.add("[title]");
            }
            if (arg.equalsIgnoreCase("station")) {
                String sub = args[1].toLowerCase();
                if (sub.equals("open") || sub.equals("edit") || sub.equals("remove")) {
                    result.addAll(plugin.getStationManager().getStationIds(false));
                } else if (sub.equals("bindnpc")) {
                    result.addAll(plugin.getStationManager().getStationIds(true));
                } else if (sub.equals("create")) {
                    result.add("<stationID>");
                } else if (sub.equals("npc")) {
                    result.add("[id|-]");
                }
            }
        } else if (args.length == 4) {
            String arg = args[0].toLowerCase();
            if (arg.equalsIgnoreCase("station")) {
                String sub = args[1].toLowerCase();
                if (sub.equals("open")) {
                    result.add("[player]");
                } else if (sub.equals("create")) {
                    result.add("[title]");
                } else if (sub.equals("npc")) {
                    result.add("[title]");
                }
            }
        }

        return result;
    }
}
