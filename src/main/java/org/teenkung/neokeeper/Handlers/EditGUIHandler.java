package org.teenkung.neokeeper.Handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryManager;
import org.teenkung.neokeeper.NeoKeeper;

public class EditGUIHandler implements Listener {

    private final NeoKeeper plugin;

    public EditGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (EditInventoryManager.isFromEditInventory(event.getClickedInventory())) {
            Inventory inv = event.getClickedInventory();
            if (event.getSlot() == 53 ) {
                String id = EditInventoryManager.getStorage(inv).id();
                plugin.getShopLoader().getTradeManager(id).saveEdit(inv);
                event.getWhoClicked().closeInventory();
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (EditInventoryManager.isFromEditInventory(event.getInventory())) EditInventoryManager.removeInventory(event.getInventory());
    }

}
