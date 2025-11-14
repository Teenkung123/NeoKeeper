package org.teenkung.neokeeper.Commands.SubCommands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.CitizensUtils;

import java.util.Arrays;

public class StationCommand {

    public void execute(NeoKeeper plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (args.length < 2) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper station <open|edit|create|remove|npc|bindnpc>"));
            return;
        }
        String action = args[1].toLowerCase();
        switch (action) {
            case "open" -> handleOpen(plugin, sender, args);
            case "edit" -> handleEdit(plugin, sender, args);
            case "create" -> handleCreate(plugin, sender, args);
            case "remove" -> handleRemove(plugin, sender, args);
            case "npc" -> handleNpc(plugin, sender, args);
            case "bindnpc" -> handleBindNpc(plugin, sender, args);
            default -> sender.sendMessage(plugin.colorize("<red>Unknown station action. Use open, edit, create, remove, npc, or bindnpc."));
        }
    }

    private void handleOpen(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper station open <stationID> [player]"));
            return;
        }
        String id = args[2];
        StationDefinition station = plugin.getStationManager().getStation(id);
        if (station == null) {
            sender.sendMessage(plugin.colorize("<red>Station <yellow>" + id + "<red> not found."));
            return;
        }
        Player target;
        if (args.length >= 4) {
            target = Bukkit.getPlayer(args[3]);
            if (target == null) {
                sender.sendMessage(plugin.colorize("<red>Player <yellow>" + args[3] + "<red> is not online."));
                return;
            }
        } else {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(plugin.colorize("<red>Only players can open stations without specifying a target."));
                return;
            }
            target = player;
        }
        station.getPlayerListGUI().open(target, 0);
    }

    private void handleEdit(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("<red>Only players can edit stations."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper station edit <stationID>"));
            return;
        }
        String id = args[2];
        StationDefinition station = plugin.getStationManager().getStation(id);
        if (station == null) {
            sender.sendMessage(plugin.colorize("<red>Station <yellow>" + id + "<red> not found."));
            return;
        }
        station.getEditorListGUI().open(player, 0);
    }

    private void handleCreate(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper station create <stationID> [title]"));
            return;
        }
        String id = args[2];
        if (plugin.getStationManager().getStation(id) != null) {
            sender.sendMessage(plugin.colorize("<red>Station <yellow>" + id + "<red> already exists."));
            return;
        }
        String title = args.length > 3 ? String.join(" ", java.util.Arrays.copyOfRange(args, 3, args.length)) : "Station";
        StationDefinition station = plugin.getStationManager().createStation(id, title);
        if (station == null) {
            sender.sendMessage(plugin.colorize("<red>Failed to create station <yellow>" + id + "<red>. Check console for details."));
            return;
        }
        sender.sendMessage(plugin.colorize("<green>Station <yellow>" + id + "<green> created."));
    }

    private void handleRemove(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(plugin.colorize("<red>Usage: /neokeeper station remove <stationID>"));
            return;
        }
        String id = args[2];
        if (plugin.getStationManager().deleteStation(id)) {
            sender.sendMessage(plugin.colorize("<green>Removed station <yellow>" + id + "<green>."));
        } else {
            sender.sendMessage(plugin.colorize("<red>Station <yellow>" + id + "<red> was not found or could not be removed."));
        }
    }

    private void handleNpc(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            sender.sendMessage(plugin.colorize("<red>Citizens plugin is not available. Station NPCs are disabled."));
            return;
        }
        String requestedId = null;
        String suppliedTitle = null;
        if (args.length > 2) {
            String firstExtra = args[2];
            if ("-".equals(firstExtra)) {
                if (args.length > 3) {
                    suppliedTitle = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();
                }
            } else {
                requestedId = firstExtra;
                if (args.length > 3) {
                    suppliedTitle = String.join(" ", Arrays.copyOfRange(args, 3, args.length)).trim();
                }
            }
            if (suppliedTitle != null && suppliedTitle.isBlank()) {
                suppliedTitle = null;
            }
        }
        if (requestedId == null && suppliedTitle == null && args.length > 2) {
            suppliedTitle = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
            if (suppliedTitle.isEmpty()) {
                suppliedTitle = null;
            }
        }
        if (requestedId != null && plugin.getStationManager().getStation(requestedId) != null) {
            sender.sendMessage(plugin.colorize("<red>Station <yellow>" + requestedId + "<red> already exists. Choose a different id."));
            return;
        }
        StationDefinition station = requestedId != null
                ? plugin.getStationManager().createNpcStation(suppliedTitle, requestedId)
                : plugin.getStationManager().createNpcStation(suppliedTitle);
        if (station == null) {
            sender.sendMessage(plugin.colorize("<red>Failed to create station. Check console for details."));
            return;
        }
        String baseTitle = suppliedTitle != null ? suppliedTitle : station.getPlainTitle();
        String npcDisplayName = PlainTextComponentSerializer.plainText().serialize(plugin.colorize(baseTitle));
        npcDisplayName = npcDisplayName.replaceAll("[^A-Za-z0-9_ ]", "").trim();
        if (npcDisplayName.isEmpty()) {
            npcDisplayName = "Station";
        }
        if (npcDisplayName.length() > 16) {
            npcDisplayName = npcDisplayName.substring(0, 16);
        }

        CitizensUtils.StationCreationResult result = citizensUtils.createStationNPC(player, npcDisplayName, station.getId());
        if (result == null) {
            plugin.getStationManager().deleteStation(station.getId());
            sender.sendMessage(plugin.colorize("<red>Failed to spawn Citizens NPC. Check console for details."));
            return;
        }

        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>Created station NPC <yellow>" + result.name() + "<green> with station ID <yellow>" + station.getId()));
        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <gray>Shift-right-click the NPC to edit this station."));
        station.getEditorListGUI().open(player, 0);
    }

    private void handleBindNpc(NeoKeeper plugin, CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            sender.sendMessage(plugin.colorize("<red>Citizens plugin is not available. NPC binding is disabled."));
            return;
        }
        if (args.length < 3) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Usage: /neokeeper station bindnpc <stationID>"));
            return;
        }
        String stationId = args[2];
        StationDefinition station = plugin.getStationManager().getStation(stationId);
        if (station == null) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Unknown station id <yellow>" + stationId + "<red>."));
            return;
        }
        NPC selected;
        try {
            selected = CitizensAPI.getDefaultNPCSelector().getSelected(sender);
        } catch (Throwable throwable) {
            plugin.getLogger().warning("Failed to access Citizens NPC selector: " + throwable.getMessage());
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Could not determine the selected NPC. Try /npc select."));
            return;
        }

        if (selected == null) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Select an NPC first (right-click it or use /npc select)."));
            return;
        }

        citizensUtils.getNpcIdForStation(stationId).ifPresent(existingNpcId -> {
            if (existingNpcId != selected.getId()) {
                citizensUtils.unregisterStationNpc(existingNpcId);
            }
        });

        boolean bound = citizensUtils.bindExistingStationNpc(selected, stationId);
        if (!bound) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Failed to bind the selected NPC. Check console for details."));
            return;
        }

        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>NPC <yellow>" + selected.getName() + "<green> now serves station <yellow>" + stationId + "<green>."));
        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <gray>Shift-right-click the NPC to edit this station."));
    }
}
