package org.teenkung.neokeeper.GUIs;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryManager;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EditGUI {

    private final InventoryManager inventoryManager;
    private final NeoKeeper plugin;
    private final ItemStack saveItem;
    private final ItemStack editItem;
    private final ItemStack deleteItem;

    public EditGUI(InventoryManager shopManager) {
        this.inventoryManager = shopManager;
        this.plugin = shopManager.getPlugin();

        // Create control items once
        saveItem = createControlItem(Material.LIME_CONCRETE, "<green>Save Changes", List.of(
                "<white>Save the changes you made",
                "<white>to the shop.",
                "",
                "<yellow>Please make sure that the reward items are on the top of each column.",
                "<green>Click to save."
        ));

        editItem = createControlItem(Material.YELLOW_CONCRETE, "<yellow>Change GUI Title", List.of(
                "<white>Change the GUI title.",
                "<white>Current Title: <reset>" + shopManager.getConfig().getString("Option.Title", "Default Shop"),
                "",
                "<green>Click to change."
        ));

        deleteItem = createControlItem(Material.RED_CONCRETE, "<red>Delete", List.of(
                "<white>Delete this shop.",
                "<red>Warning: This action is irreversible.",
                "",
                "<red>Click to delete."
        ));
    }

    private ItemStack createControlItem(Material material, String displayName, List<String> loreLines) {
        ItemManager itemManager = new ItemManager(new ItemStack(material));
        itemManager.setDisplayName(plugin.colorize(displayName));
        List<Component> loreComponents = new ArrayList<>();
        for (String line : loreLines) {
            loreComponents.add(plugin.colorize(line));
        }
        itemManager.setLore(loreComponents);
        return itemManager.getItem();
    }

    public void buildEditGUI(Player player) {
        Inventory inv = createEditGUI();
        player.openInventory(inv);
    }

    public Inventory createEditGUI() {
        String id = inventoryManager.getId();
        Inventory inv = EditInventoryManager.createInventory(54, Component.text("Editing: " + id), id);

        // Place control items in the inventory
        inv.setItem(35, saveItem);
        inv.setItem(44, editItem);
        inv.setItem(53, deleteItem);

        // Populate the inventory with trade items
        List<TradeManager> tradeManagers = inventoryManager.getTradeManagers();
        int maxTradesPerPage = 18; // 54 slots / 3 slots per trade
        int tradeCount = Math.min(tradeManagers.size(), maxTradesPerPage);

        for (int i = 0; i < tradeCount; i++) {
            TradeManager tradeSet = tradeManagers.get(i);

            int column = i % 9;
            int rowGroup = i / 9; // 0 or 1
            int rowOffset = rowGroup * 27;

            int rewardSlot = rowOffset + column;
            int q1Slot = rowOffset + 9 + column;
            int q2Slot = rowOffset + 18 + column;

            inv.setItem(rewardSlot, tradeSet.getRewardItem());
            inv.setItem(q1Slot, tradeSet.getQuest1Item());
            inv.setItem(q2Slot, tradeSet.getQuest2Item());
        }

        return inv;
    }

    public void saveEdit(Inventory inv) {
        YamlConfiguration config = inventoryManager.getConfig();
        String id = inventoryManager.getId();
        config.set("Items", null);
        int tradeIndex = 0;

        for (int i = 0; i < 17; i++) {
            int column = i % 9;
            int row = Double.valueOf(Math.floor(i / 9D)).intValue(); // 0 or 1

            int rewardSlot = column + (row*27);
            int q1Slot = column + 9 + (row*27);
            int q2Slot = column + 18 + (row*27);

            ItemStack rewardItem = inv.getItem(rewardSlot);
            ItemStack q1Item = inv.getItem(q1Slot);
            ItemStack q2Item = inv.getItem(q2Slot);

            if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                continue;
            }

            ConfigurationSection section = config.createSection("Items." + tradeIndex);
            ItemManager rewardItemManager = new ItemManager(rewardItem);
            ItemManager q1ItemManager = new ItemManager(q1Item);
            ItemManager q2ItemManager = new ItemManager(q2Item);

            setSection(section, "Reward", rewardItemManager);
            setSection(section, "Quests.1", q1ItemManager);
            setSection(section, "Quests.2", q2ItemManager);

            tradeIndex++;
        }

//        for (int i = 0; i < 54; i++) {
//
//            int column = i % 9;
//            int rowGroup = Double.valueOf(Math.floor(i / 27D)).intValue(); // 0 or 1
//            int rowOffset = rowGroup * 27;
//
//            if (column == 8 && rowGroup == 1) {
//                continue;
//            }
//
//            int rewardSlot = rowOffset + column;
//            int q1Slot = rowOffset + 9 + column;
//            int q2Slot = rowOffset + 18 + column;
//
//            ItemStack rewardItem = inv.getItem(rewardSlot);
//            if (rewardItem == null || rewardItem.getType() == Material.AIR) {
//                continue;
//            }
//
//            ItemStack q1Item = inv.getItem(q1Slot);
//            ItemStack q2Item = inv.getItem(q2Slot);
//
//            ConfigurationSection section = config.createSection("Items." + tradeIndex);
//            ItemManager rewardItemManager = new ItemManager(rewardItem);
//            ItemManager q1ItemManager = new ItemManager(q1Item);
//            ItemManager q2ItemManager = new ItemManager(q2Item);
//
//            setSection(section, "Reward", rewardItemManager);
//            setSection(section, "Quests.1", q1ItemManager);
//            setSection(section, "Quests.2", q2ItemManager);
//
//            tradeIndex++;
//        }

        try {
            config.save(new File(plugin.getDataFolder(), "Shops/" + id + ".yml"));
            plugin.reload();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save shop configuration for " + id);
            e.printStackTrace();
        }
    }

    private void setSection(ConfigurationSection section, String path, ItemManager item) {
        section.set(path + ".Type", item.getType());
        section.set(path + ".Item", item.getStringItem());
        section.set(path + ".Amount", item.getAmount());
    }
}
