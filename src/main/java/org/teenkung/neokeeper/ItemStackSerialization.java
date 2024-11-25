package org.teenkung.neokeeper;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import java.util.Map;

public class ItemStackSerialization {

    private static YamlConfiguration config = new YamlConfiguration();
    /**
     * Serializes an ItemStack to a String.
     *
     * @param item the ItemStack to serialize
     * @return the serialized ItemStack as a String
     */
    public static String serialize(ItemStack item) {
        config.set("item", item);
        return config.saveToString();
    }

    /**
     * Deserializes an ItemStack from a String.
     *
     * @param itemString the String to deserialize
     * @return the deserialized ItemStack
     */
    public static ItemStack deserialize(String itemString) {
        try {
            config.loadFromString(itemString);
            return config.getItemStack("item");
        } catch (Exception e) {
            return new ItemStack(Material.AIR);
        }
    }

}

