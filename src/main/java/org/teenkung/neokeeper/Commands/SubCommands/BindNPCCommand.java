package org.teenkung.neokeeper.Commands.SubCommands;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.CitizensUtils;

public class BindNPCCommand {

    public void execute(NeoKeeper plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }

        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            sender.sendMessage(plugin.colorize("<red>Citizens plugin is not available. NPC binding is disabled."));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Usage: /neokeeper bindnpc <shopID>"));
            return;
        }

        String shopId = args[1];
        InventoryManager manager = plugin.getShopManager().getTradeManager(shopId);
        if (manager == null) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Unknown shop id <yellow>" + shopId + "<red>."));
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

        citizensUtils.getNpcIdForShop(shopId).ifPresent(existingNpcId -> {
            if (existingNpcId != selected.getId()) {
                citizensUtils.unregisterShopkeeper(existingNpcId);
            }
        });

        boolean bound = citizensUtils.bindExistingShopkeeper(selected, shopId);
        if (!bound) {
            sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <red>Failed to bind the selected NPC. Check console for details."));
            return;
        }

        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>NPC <yellow>" + selected.getName() + "<green> now serves shop <yellow>" + shopId + "<green>."));
        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <gray>Shift-right-click the NPC to edit this shop."));
    }
}
