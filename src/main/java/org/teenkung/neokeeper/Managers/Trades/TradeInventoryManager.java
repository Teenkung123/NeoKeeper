package org.teenkung.neokeeper.Managers.Trades;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.inventory.Inventory;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryStorage;

import java.util.HashMap;
import java.util.Map;

public class TradeInventoryManager {
    private static final Map<Inventory, TradeInventoryStorage> registeredInventories = new HashMap<>();
    private static Integer offset;
    private static String id;

    public static Inventory createPluginInventory(int size, Component title, String id) {
        Inventory inv = Bukkit.createInventory(null, size, title);
        registeredInventories.put(inv, new TradeInventoryStorage(id, 0));
        return inv;
    }

    public static boolean isPluginInventory(Inventory inventory) {
        return registeredInventories.containsKey(inventory);
    }

    public static void removeInventory(Inventory inventory) {
        registeredInventories.remove(inventory);
    }

    public static TradeInventoryStorage getInventoryStorage(Inventory inventory) { return registeredInventories.getOrDefault(inventory, null); }

    public static void setOffset(Inventory inventory, Integer offset) { registeredInventories.get(inventory).offset(offset); }
    public static Integer getOffset() { return offset; }
    public static String getID() { return id; }

    public static Map<Inventory, TradeInventoryStorage> getAllInventories() { return registeredInventories; }

    // Remember to remove inventories from the set when they're closed or no longer needed
}
