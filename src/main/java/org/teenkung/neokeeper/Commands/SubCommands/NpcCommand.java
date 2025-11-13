package org.teenkung.neokeeper.Commands.SubCommands;

import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.CitizensUtils;

import java.util.Arrays;

public class NpcCommand {

    public void execute(NeoKeeper plugin, @NotNull CommandSender sender, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(plugin.colorize("<red>Only players can use this command!"));
            return;
        }

        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            sender.sendMessage(plugin.colorize("<red>Citizens plugin is not available. NPC shops are disabled."));
            return;
        }

        String suppliedTitle = null;
        if (args.length > 1) {
            suppliedTitle = String.join(" ", Arrays.copyOfRange(args, 1, args.length)).trim();
            if (suppliedTitle.isEmpty()) {
                suppliedTitle = null;
            }
        }

        InventoryManager manager = plugin.getShopManager().createNpcShop(suppliedTitle);
        if (manager == null) {
            sender.sendMessage(plugin.colorize("<red>Failed to create NPC shop. Check console for details."));
            return;
        }

        String baseTitle = suppliedTitle != null ? suppliedTitle : plugin.getConfig().getString("GUI.DefaultTitle", "Default Shop");
        String npcDisplayName = PlainTextComponentSerializer.plainText().serialize(plugin.colorize(baseTitle));
        npcDisplayName = npcDisplayName.replaceAll("[^A-Za-z0-9_ ]", "").trim();
        if (npcDisplayName.isEmpty()) {
            npcDisplayName = "Shopkeeper";
        }
        if (npcDisplayName.length() > 16) {
            npcDisplayName = npcDisplayName.substring(0, 16);
        }

        CitizensUtils.ShopkeeperCreationResult result = citizensUtils.createShopkeeperNPC(player, npcDisplayName, manager.getId());
        if (result == null) {
            plugin.getShopManager().deleteShop(manager.getId(), true);
            sender.sendMessage(plugin.colorize("<red>Failed to spawn Citizens NPC. Check console for details."));
            return;
        }

        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <green>Created shopkeeper NPC <yellow>" + result.name() + "<green> with hidden shop ID <yellow>" + manager.getId()));
        sender.sendMessage(plugin.colorize(plugin.getPrefix() + "  <gray>Shift-right-click the NPC to edit this shop."));

        manager.getEditGUI().openEditGUI(player);
    }
}
