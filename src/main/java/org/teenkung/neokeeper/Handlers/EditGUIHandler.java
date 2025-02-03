package org.teenkung.neokeeper.Handlers;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.teenkung.neokeeper.GUIs.EditGUI;
import org.teenkung.neokeeper.GUIs.EditGUI.EditInventoryHolder;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.HashMap;
import java.util.Map;

public class EditGUIHandler implements Listener {
    private final NeoKeeper plugin;

    // A map to track players that are renaming their shop.
    private final Map<Player, String> renaming = new HashMap<>();

    public EditGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInv = event.getClickedInventory();
        if (clickedInv == null) return;
        InventoryHolder holder = clickedInv.getHolder();
        if (!(holder instanceof EditInventoryHolder(String shopId, int page))) return;

        // Get metadata from our custom holder.
        // Retrieve the EditGUI instance from your TradeManager.
        EditGUI editGUI = plugin.getShopManager().getTradeManager(shopId).getEditGUI();
        Player player = (Player) event.getWhoClicked();
        int slot = event.getSlot();

        // We assume that control buttons are in row 4 (slots 27-35). Cancel these clicks.
        if (slot >= 27 && slot < 36) {
            event.setCancelled(true);
            if (slot == 27) { // Previous page
                if (page > 0) {
                    editGUI.openEditGUI(player, page - 1);
                }
            } else if (slot == 35) { // Next page
                editGUI.openEditGUI(player, page + 1);
            } else if (slot == 30) { // Rename button
                renaming.put(player, shopId);
                player.closeInventory();
                player.sendMessage(plugin.colorize("<yellow>Please type the new shop title in chat."));
            } else if (slot == 31) { // Save button
                editGUI.saveAllPages();
                player.closeInventory();
                player.sendMessage(plugin.colorize("<green>Shop saved successfully."));
            } else if (slot == 32) { // Delete button
                player.closeInventory();
                if (plugin.getShopManager().deleteShop(shopId, true)) {
                    player.sendMessage(plugin.colorize("<green>Shop deleted successfully."));
                } else {
                    player.sendMessage(plugin.colorize("<red>Failed to delete shop. Check console for details."));
                }
            }
        } else {
            // Allow interaction with the trade area (rows 1–3) so the player can move items.
            event.setCancelled(false);
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        // Optionally you can clean up or persist page cache here if the editing session is over.
        // In this example the EditGUI instance (held by the TradeManager) keeps its page cache
        // until a save or deletion occurs.
    }

    @EventHandler
    public void onAsyncPlayerChat(AsyncChatEvent event) {
        Player player = event.getPlayer();
        if (renaming.containsKey(player)) {
            event.setCancelled(true);
            String shopId = renaming.remove(player);
            // Change the title in your configuration.
            plugin.getShopManager().getTradeManager(shopId).getEditGUI().inventoryManager
                    .getConfig().set("Option.Title", event.message());
            player.sendMessage(plugin.colorize("<green>Shop title updated to: " + event.message()));
            // Optionally, you might save or reload the config immediately.
        }
    }
}
