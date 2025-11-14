package org.teenkung.neokeeper.Managers.Stations;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.ItemManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * Represents a single station recipe. Each recipe can contain an arbitrary amount of input
 * materials with a single result item.
 */
public class StationRecipe {

    private final String key;
    private final ItemManager result;
    private final List<ItemManager> materials;

    public StationRecipe(String key, ConfigurationSection section) {
        this.key = key;
        this.result = loadItem(section.getConfigurationSection("Result"));
        this.materials = loadMaterials(section.getConfigurationSection("Materials"));
    }

    public String getKey() {
        return key;
    }

    public ItemManager getResultManager() {
        return result;
    }

    public ItemStack getResultItem() {
        return result.getItem();
    }

    public List<ItemManager> getMaterials() {
        return materials;
    }

    private ItemManager loadItem(ConfigurationSection section) {
        if (section == null) {
            return new ItemManager("NONE", "NONE", 0);
        }
        return new ItemManager(
                section.getString("Type", "NONE"),
                section.getString("Item", "NONE"),
                section.getInt("Amount", 0)
        );
    }

    private List<ItemManager> loadMaterials(ConfigurationSection section) {
        if (section == null) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>(section.getKeys(false));
        keys.sort(Comparator.comparingInt(key -> {
            try {
                return Integer.parseInt(key);
            } catch (NumberFormatException ignored) {
                return Integer.MAX_VALUE;
            }
        }));
        List<ItemManager> loaded = new ArrayList<>();
        for (String key : keys) {
            ConfigurationSection materialSection = section.getConfigurationSection(key);
            if (materialSection == null) {
                continue;
            }
            ItemManager item = loadItem(materialSection);
            if (!Objects.equals(item.getType(), "NONE")) {
                loaded.add(item);
            }
        }
        return Collections.unmodifiableList(loaded);
    }
}
