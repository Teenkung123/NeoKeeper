package org.teenkung.neokeeper.Managers;

import org.bukkit.configuration.file.YamlConfiguration;

import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class InventoriesLoader {

    private NeoKeeper plugin;
    private final File shopsFolder;
    private Map<String, InventoryManager> tradeUtils;

    public InventoriesLoader(NeoKeeper plugin) {

        this.plugin = plugin;
        this.shopsFolder = new File(plugin.getDataFolder(), "Shops");
    }

    public void loadAllShop() {
        this.tradeUtils = new HashMap<>();
        if (!shopsFolder.exists()) {
            plugin.getLogger().info("Shops folder does not exist. Creating new one...");
            if (!shopsFolder.mkdirs()) { // Attempt to create the folder
                plugin.getLogger().severe("Could not create shops folder. Please check permissions.");
                return;
            }
        }

        File[] files = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) {
            plugin.getLogger().warning("Failed to list files in shops folder.");
            return;
        }

        for (File file : files) {
            String fileNameWithoutExtension = file.getName().replaceAll("\\.(yml|yaml)$", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            tradeUtils.put(fileNameWithoutExtension, new InventoryManager(plugin, config, fileNameWithoutExtension));
            plugin.getLogger().info("Loaded shop config: " + fileNameWithoutExtension);
        }

        plugin.getLogger().info("Loaded all shop configs. Total: " + tradeUtils.size());
    }

    public Map<String, InventoryManager> getAllTradeManagers() { return tradeUtils; }
    public InventoryManager getTradeManager(String id) { return tradeUtils.getOrDefault(id, null); }


    public void addShop(String id, String name) {
        if (!shopsFolder.exists()) {
            plugin.getLogger().info("Shops folder does not exist. Creating new one...");
            if (!shopsFolder.mkdirs()) {
                plugin.getLogger().severe("Could not create shops folder. Please check permissions.");
                return;
            }
        }

        File newShopFile = new File(shopsFolder, id + ".yml");
        if (!newShopFile.exists()) {
            try {
                if (!newShopFile.createNewFile()) {
                    plugin.getLogger().severe("Could not create the shop file for ID: " + id);
                    return;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(newShopFile);
                config.createSection("Option");
                config.set("Option.Title", name);
                config.createSection("Items"); // Create the "Items" section
                config.save(newShopFile);
                InventoryManager newShop = new InventoryManager(plugin, config, id);
                tradeUtils.put(id, newShop);
                plugin.getLogger().info("New shop added with ID: " + id);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create the shop file for ID: " + id + ". Error: " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("Shop with ID: " + id + " already exists.");
        }
    }

    public void removeShop(String id) {
        File shopFile = new File(shopsFolder, id + ".yml");
        if (shopFile.exists()) {
            if (shopFile.delete()) {
                tradeUtils.remove(id);
                plugin.getLogger().info("Shop removed with ID: " + id);
            } else {
                plugin.getLogger().severe("Could not delete the shop file for ID: " + id);
            }
        } else {
            plugin.getLogger().warning("Shop with ID: " + id + " does not exist.");
        }
    }
}
