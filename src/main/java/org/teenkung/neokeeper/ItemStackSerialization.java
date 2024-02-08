package org.teenkung.neokeeper;

import org.bukkit.inventory.ItemStack;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.Material;
import java.util.Map;

public class ItemStackSerialization {

    /**
     * Serializes an ItemStack to a String.
     *
     * @param item the ItemStack to serialize
     * @return the serialized ItemStack as a String
     */
    public static String serialize(ItemStack item) {
        YamlConfiguration config = new YamlConfiguration();
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
        YamlConfiguration config = new YamlConfiguration();
        try {
            config.loadFromString(itemString);
            return config.getItemStack("item");
        } catch (Exception e) {
            return new ItemStack(Material.AIR); // Return an empty item stack in case of error
        }
    }

}

