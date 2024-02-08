package org.teenkung.neokeeper.Managers;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InventoryManager {
    private static final Set<Inventory> registeredInventories = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static Inventory createPluginInventory(int size, Component title) {
        Inventory inv = Bukkit.createInventory(null, size, title);
        registeredInventories.add(inv);
        return inv;
    }

    public static boolean isPluginInventory(Inventory inventory) {
        return registeredInventories.contains(inventory);
    }

    public static void removeInventory(Inventory inventory) {
        registeredInventories.remove(inventory);
    }

    // Remember to remove inventories from the set when they're closed or no longer needed
}
