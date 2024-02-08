package org.teenkung.neokeeper;

import org.bukkit.configuration.file.YamlConfiguration;

import org.teenkung.neokeeper.Managers.ShopManager;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class ShopLoader {

    private NeoKeeper plugin;
    private Map<String, ShopManager> shopConfigs;
    private final File shopsFolder;

    public ShopLoader(NeoKeeper plugin) {

        this.plugin = plugin;
        this.shopsFolder = new File(plugin.getDataFolder(), "Shops");
    }

    public void loadAllShop() {
        this.shopConfigs = new HashMap<>();
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
            shopConfigs.put(fileNameWithoutExtension, new ShopManager(plugin, YamlConfiguration.loadConfiguration(file), fileNameWithoutExtension));
            plugin.getLogger().info("Loaded shop config: " + fileNameWithoutExtension);
        }

        plugin.getLogger().info("Loaded all shop configs. Total: " + shopConfigs.size());
    }

    public boolean addShop(String id) {
        if (!shopsFolder.exists()) {
            plugin.getLogger().info("Shops folder does not exist. Creating new one...");
            if (!shopsFolder.mkdirs()) {
                plugin.getLogger().severe("Could not create shops folder. Please check permissions.");
                return false;
            }
        }

        File newShopFile = new File(shopsFolder, id + ".yml");
        if (!newShopFile.exists()) {
            try {
                if (!newShopFile.createNewFile()) {
                    plugin.getLogger().severe("Could not create the shop file for ID: " + id);
                    return false;
                }
                YamlConfiguration config = YamlConfiguration.loadConfiguration(newShopFile);
                config.createSection("Items"); // Create the "Items" section
                config.save(newShopFile);
                ShopManager newShop = new ShopManager(plugin, config, id);
                shopConfigs.put(id, newShop);
                plugin.getLogger().info("New shop added with ID: " + id);
                return true;
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create the shop file for ID: " + id + ". Error: " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("Shop with ID: " + id + " already exists.");
        }
        return false;
    }

    public boolean removeShop(String id) {
        File shopFile = new File(shopsFolder, id + ".yml");
        if (shopFile.exists()) {
            if (shopFile.delete()) {
                shopConfigs.remove(id);
                plugin.getLogger().info("Shop removed with ID: " + id);
                return true;
            } else {
                plugin.getLogger().severe("Could not delete the shop file for ID: " + id);
            }
        } else {
            plugin.getLogger().warning("Shop with ID: " + id + " does not exist.");
        }
        return false;
    }

    public Map<String, ShopManager> getAllShopManagers() { return shopConfigs; }
    public ShopManager getShopManager(String id) { return shopConfigs.getOrDefault(id, null); }
}
