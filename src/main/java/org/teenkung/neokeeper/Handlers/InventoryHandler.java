package org.teenkung.neokeeper.Handlers;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.teenkung.neokeeper.Managers.InventoryManager;

public class InventoryHandler implements Listener {

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (InventoryManager.isPluginInventory(event.getInventory())) {
            InventoryManager.removeInventory(event.getInventory());
        }
    }

}
