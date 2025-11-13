package org.teenkung.neokeeper.Managers;

import org.bukkit.configuration.file.YamlConfiguration;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class ShopManager {

    private static final String NPC_ID_PREFIX = "npc_";

    private final NeoKeeper plugin;
    private final File shopsFolder;
    private final Set<String> hiddenShopIds;
    private Map<String, InventoryManager> tradeUtils;

    public ShopManager(NeoKeeper plugin) {
        this.plugin = plugin;
        this.shopsFolder = new File(plugin.getDataFolder(), "Shops");
        this.hiddenShopIds = new LinkedHashSet<>();
        this.tradeUtils = new HashMap<>();
    }

    public void loadAllShop() {
        this.tradeUtils = new HashMap<>();
        this.hiddenShopIds.clear();
        if (!ensureFolder()) {
            return;
        }

        File[] files = shopsFolder.listFiles((dir, name) -> name.endsWith(".yml") || name.endsWith(".yaml"));
        if (files == null) {
            plugin.getLogger().warning("Failed to list files in shops folder.");
            return;
        }

        for (File file : files) {
            String fileNameWithoutExtension = file.getName().replaceAll("\\.(yml|yaml)$", "");
            YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
            InventoryManager manager = new InventoryManager(plugin, config, fileNameWithoutExtension);
            tradeUtils.put(fileNameWithoutExtension, manager);
            if (manager.isHidden()) {
                hiddenShopIds.add(fileNameWithoutExtension);
            }
            plugin.getLogger().info("Loaded shop config: " + fileNameWithoutExtension);
        }

        plugin.getLogger().info("Loaded all shop configs. Total: " + tradeUtils.size());
    }

    public Map<String, InventoryManager> getAllTradeManagers() {
        return tradeUtils;
    }

    public InventoryManager getTradeManager(String id) {
        return tradeUtils.getOrDefault(id, null);
    }

    public Set<String> getVisibleShopIds() {
        return tradeUtils.keySet().stream()
                .filter(id -> !hiddenShopIds.contains(id))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    public boolean isHidden(String id) {
        return hiddenShopIds.contains(id);
    }

    public InventoryManager addShop(String id, String title) {
        return addShopInternal(id, title, false);
    }

    public InventoryManager createNpcShop(String title) {
        String resolvedTitle = title;
        if (resolvedTitle == null || resolvedTitle.isBlank()) {
            resolvedTitle = plugin.getConfig().getString("GUI.DefaultTitle", "Default Shop");
        }

        String id = generateNpcShopId();
        int attempts = 0;
        while (tradeUtils.containsKey(id) && attempts++ < 10) {
            id = generateNpcShopId();
        }

        if (tradeUtils.containsKey(id)) {
            plugin.getLogger().severe("Failed to generate a unique NPC shop id after multiple attempts.");
            return null;
        }

        return addShopInternal(id, resolvedTitle, true);
    }

    private String generateNpcShopId() {
        return NPC_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, 12);
    }

    private InventoryManager addShopInternal(String id, String title, boolean hidden) {
        if (!ensureFolder()) {
            return null;
        }

        File newShopFile = new File(shopsFolder, id + ".yml");
        if (newShopFile.exists()) {
            plugin.getLogger().warning("Shop with ID: " + id + " already exists.");
            return tradeUtils.get(id);
        }

        try {
            if (!newShopFile.createNewFile()) {
                plugin.getLogger().severe("Could not create the shop file for ID: " + id);
                return null;
            }

            YamlConfiguration config = YamlConfiguration.loadConfiguration(newShopFile);
            config.createSection("Option");
            config.set("Option.Title", title);
            config.set("Option.Hidden", hidden);
            config.createSection("Items");
            config.save(newShopFile);

            InventoryManager newShop = new InventoryManager(plugin, config, id);
            tradeUtils.put(id, newShop);
            if (hidden) {
                hiddenShopIds.add(id);
            }
            plugin.getLogger().info("New " + (hidden ? "hidden " : "") + "shop added with ID: " + id);
            return newShop;
        } catch (IOException e) {
            plugin.getLogger().severe("Could not create the shop file for ID: " + id + ". Error: " + e.getMessage());
            return null;
        }
    }

    public boolean deleteShop(String id, boolean removeFiles) {
        if (!tradeUtils.containsKey(id)) {
            plugin.getLogger().warning("Shop with ID: " + id + " does not exist.");
            return false;
        }

        tradeUtils.remove(id);
        hiddenShopIds.remove(id);

        if (removeFiles) {
            File shopFile = new File(shopsFolder, id + ".yml");
            if (shopFile.exists() && !shopFile.delete()) {
                plugin.getLogger().severe("Could not delete the shop file for ID: " + id);
                return false;
            }
        }

        plugin.getLogger().info("Shop removed with ID: " + id);
        return true;
    }

    private boolean ensureFolder() {
        if (shopsFolder.exists()) {
            return true;
        }

        plugin.getLogger().info("Shops folder does not exist. Creating new one...");
        if (!shopsFolder.mkdirs()) {
            plugin.getLogger().severe("Could not create shops folder. Please check permissions.");
            return false;
        }
        return true;
    }
}

