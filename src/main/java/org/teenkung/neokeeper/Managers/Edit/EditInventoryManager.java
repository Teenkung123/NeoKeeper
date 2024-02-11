package org.teenkung.neokeeper.Managers.Edit;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;

import java.util.HashMap;
import java.util.Map;

public class EditInventoryManager {

    private static final Map<Inventory, EditInventoryStorage> registeredInventories = new HashMap<>();

    public static Inventory createInventory(Integer size, Component title, String id) {
        Inventory inv = Bukkit.createInventory(null, size, title);
        registeredInventories.put(inv, new EditInventoryStorage(id));
        return inv;
    }

    public static boolean isFromEditInventory(Inventory inv) {
        return registeredInventories.containsKey(inv);
    }

    public static void removeInventory(Inventory inv) {
        registeredInventories.remove(inv);
    }

    public static EditInventoryStorage getStorage(Inventory inv) {
        return registeredInventories.getOrDefault(inv, null);
    }

    public static Map<Inventory, EditInventoryStorage> getAllInventories() { return registeredInventories; }

}
