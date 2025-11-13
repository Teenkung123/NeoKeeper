package org.teenkung.neokeeper.GUIs;

import de.tr7zw.nbtapi.NBT;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Utils.ExcellentCratesHook;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.ArrayList;
import java.util.List;

public class TradeGUI {

    private final InventoryManager inventoryManager;
    private final NeoKeeper plugin;
    private final List<int[]> parsedSelectorSlots;
    private final ItemStack noItem;

    public TradeGUI(InventoryManager shopManager) {
        this.inventoryManager = shopManager;
        this.plugin = shopManager.getPlugin();
        this.noItem = plugin.getNoItemItem();
        this.parsedSelectorSlots = new ArrayList<>();

        // Cache parsed selector slots
        List<String> selectorSlots = plugin.getConfigLoader().getSelectorSlots();
        for (String set : selectorSlots) {
            String[] split = set.split(":");
            try {
                int q1 = Integer.parseInt(split[0]);
                int q2 = Integer.parseInt(split[1]);
                int r = Integer.parseInt(split[2]);
                parsedSelectorSlots.add(new int[]{q1, q2, r});
            } catch (NumberFormatException e) {
                plugin.getLogger().warning("Invalid selector slot format: " + set);
            }
        }
    }

    public void buildTradeGUI(Player player) {
        Inventory inventory = createGUI();
        inventory.setItem(plugin.getConfigLoader().getQuest1Slot(), null);
        inventory.setItem(plugin.getConfigLoader().getQuest2Slot(), null);
        inventory.setItem(plugin.getConfigLoader().getRewardSlot(), noItem);
        player.openInventory(inventory);
        fillSelector(player, 0);
    }

    public void fillSelector(Player player, int offset) {
        InventoryView inv = player.getOpenInventory();
        if (TradeInventoryManager.isPluginInventory(inv.getTopInventory())) {
            List<TradeManager> tradeManagers = inventoryManager.getTradeManagers();
            int tradeIndex = offset;

            for (int[] slots : parsedSelectorSlots) {
                int q1 = slots[0];
                int q2 = slots[1];
                int r = slots[2];
                if (tradeIndex < 0) return;
                if (tradeIndex >= tradeManagers.size()) {
                    inv.setItem(q1, noItem);
                    inv.setItem(q2, noItem);
                    inv.setItem(r, noItem);
                } else {
                    TradeManager tradeManager = tradeManagers.get(tradeIndex);
                    ItemStack q1Item = getOrDefault(tradeManager.getQuest1Item(), noItem);
                    ItemStack q2Item = getOrDefault(tradeManager.getQuest2Item(), noItem);
                    ItemStack rItem = getOrDefault(tradeManager.getRewardItem(), noItem);

                    int finalTradeIndex = tradeIndex;
                    NBT.modify(q1Item, nbt -> { nbt.setInteger("NeoIndex", finalTradeIndex); });
                    NBT.modify(q2Item, nbt -> { nbt.setInteger("NeoIndex", finalTradeIndex); });
                    NBT.modify(rItem, nbt -> { nbt.setInteger("NeoIndex", finalTradeIndex); });

                    inv.setItem(q1, q1Item);
                    inv.setItem(q2, q2Item);
                    inv.setItem(r, rItem);
                    tradeIndex++;
                }
            }
        }
    }

    private ItemStack getOrDefault(ItemStack item, ItemStack defaultItem) {
        return (item != null && item.getType() != Material.AIR) ? item : defaultItem;
    }

    private Inventory createGUI() {
        Component title = inventoryManager.getTitle();
        List<String> layout = plugin.getConfigLoader().getGUILayout();
        int rows = layout.size();
        String id = inventoryManager.getId();
        Inventory gui = TradeInventoryManager.createPluginInventory(rows * 9, title, id);

        ConfigurationSection section = plugin.getConfigLoader().getGUIItemsSection();
        for (String key : section.getKeys(false)) {
            String type = section.getString(key + ".Type", "VANILLA").toUpperCase();
            String item = section.getString(key + ".Item", "STONE");
            String display = section.getString(key + ".Display");
            List<String> lore = section.getStringList(key + ".Lore");
            int modelData = section.getInt(key + ".ModelData", 0);
            int amount = section.getInt(key + ".Amount", 1);

            ItemStack stack = createItemStack(type, item, display, lore, modelData, amount);

            for (int i = 0; i < rows; i++) {
                String row = layout.get(i);
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == key.charAt(0)) {
                        gui.setItem(i * 9 + j, stack);
                    }
                }
            }
        }

        return gui;
    }

    private ItemStack createItemStack(String type, String item, String display, List<String> lore, int modelData, int amount) {
        ItemStack stack = new ItemStack(Material.STONE);
        switch (type) {
            case "VANILLA":
                Material material = Material.matchMaterial(item);
                if (material == null) {
                    plugin.getLogger().warning("Material " + item + " not found.");
                    material = Material.BARRIER;
                }
                stack = new ItemStack(material, amount);
                break;

            case "MI":
                String[] typeId = item.split(":");
                if (typeId.length < 2) {
                    plugin.getLogger().warning("Invalid MMOItem identifier: " + item);
                    break;
                }
                String mmoType = typeId[0];
                String id = typeId[1];
                stack = MMOItems.plugin.getItem(mmoType, id);
                if (stack == null) {
                    plugin.getLogger().warning("MMOItem " + id + " not found.");
                    stack = new ItemStack(Material.BARRIER);
                }
                break;

            case "EC_KEY":
            case "EC":
                ItemStack keyStack = ExcellentCratesHook.getKeyItem(item);
                if (keyStack != null) {
                    stack = keyStack.clone();
                    stack.setAmount(amount);
                } else {
                    plugin.getLogger().warning("ExcellentCrates key " + item + " could not be resolved.");
                    stack = new ItemStack(Material.BARRIER);
                }
                break;

            case "IA":
                CustomStack customStack = CustomStack.getInstance(item);
                if (customStack != null) {
                    stack = customStack.getItemStack();
                } else {
                    plugin.getLogger().warning("ItemsAdder item " + item + " not found.");
                    stack = new ItemStack(Material.BARRIER);
                }
                break;

            default:
                plugin.getLogger().warning("Unknown item type: " + type);
                break;
        }

        // Set item meta
        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (display != null) {
                meta.displayName(plugin.colorize(display));
            }
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(plugin.colorize(line));
                }
                meta.lore(loreComponents);
            }
            if (modelData != 0) {
                meta.setCustomModelData(modelData);
            }
            stack.setItemMeta(meta);
        }

        // Set custom NBT tag
        NBT.modify(stack, nbt -> { nbt.setString("NeoShopID", inventoryManager.getId()); });

        return stack;
    }
}
