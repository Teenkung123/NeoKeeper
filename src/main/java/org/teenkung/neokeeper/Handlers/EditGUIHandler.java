package org.teenkung.neokeeper.Handlers;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryManager;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.HashMap;

public class EditGUIHandler implements Listener {

    private final NeoKeeper plugin;
    private final HashMap<Player, String> editing = new HashMap<>();

    public EditGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (EditInventoryManager.isFromEditInventory(event.getClickedInventory())) {
            Inventory inv = event.getClickedInventory();
            String id = EditInventoryManager.getStorage(inv).id();
            if (event.getSlot() == 35 ) {
                plugin.getShopManager().getTradeManager(id).getEditGUI().saveEdit(inv);
                event.getWhoClicked().closeInventory();
                event.getWhoClicked().sendMessage(plugin.colorize("<green>Successfully saved the shop id " + id + "!"));
            } else if (event.getSlot() == 44) {
                editing.put((Player) event.getWhoClicked(), id);
                event.getWhoClicked().closeInventory();
            } else if (event.getSlot() == 53) {
                event.getWhoClicked().closeInventory();
                if (plugin.getShopManager().deleteShop(id, true)) {
                    event.getWhoClicked().sendMessage(plugin.colorize("<green>Successfully deleted the shop id " + id + "!"));
                } else {
                    event.getWhoClicked().sendMessage(plugin.colorize("<red>Failed to delete the shop id " + id + "! Check console for more information."));
                }
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (EditInventoryManager.isFromEditInventory(event.getInventory())) EditInventoryManager.removeInventory(event.getInventory());
    }

    @EventHandler
    public void onChat(AsyncChatEvent event) {
        if (editing.containsKey(event.getPlayer())) {
            event.setCancelled(true);
            String id = editing.get(event.getPlayer());
            plugin.getShopManager().getTradeManager(id).setTitle(event.message());
            event.getPlayer().sendMessage(plugin.colorize("<green>Successfully set the title of the shop id " + id + " to ").append(event.message()));
        }
    }

}
