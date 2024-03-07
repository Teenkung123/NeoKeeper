package org.teenkung.neokeeper.Managers;

import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryManager;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class InventoryManager {

    private final YamlConfiguration config;
    FileConfiguration mainConfig;
    private final NeoKeeper plugin;
    private final String id;
    private final ArrayList<TradeManager> tradeManagers;
    private final List<String> listPerPage;
    public InventoryManager(NeoKeeper plugin, YamlConfiguration config, String id) {
        this.config = config;
        this.plugin = plugin;
        this.tradeManagers = new ArrayList<>();
        this.mainConfig = plugin.getConfig();
        this.id = id;
        this.listPerPage = mainConfig.getStringList("GUI.Slot.Lists");

        ConfigurationSection section = config.getConfigurationSection("Items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                tradeManagers.add(new TradeManager(Objects.requireNonNull(section.getConfigurationSection(key))));
            }
        }
    }

    public void buildTradeGUI(Player player) {
        Inventory inventory = createGUI();
        inventory.setItem(mainConfig.getInt("GUI.Slot.Quest1"), null);
        inventory.setItem(mainConfig.getInt("GUI.Slot.Quest2"), null);
        inventory.setItem(mainConfig.getInt("GUI.Slot.Reward"), plugin.getNoItemItem());
        player.openInventory(inventory);
        fillSelector(player, 0);
    }

    public void fillSelector(Player player, Integer offset) {
        if (TradeInventoryManager.isPluginInventory(player.getOpenInventory().getTopInventory())) {
            InventoryView inv = player.getOpenInventory();
            for (String set : listPerPage) {
                try {
                    String[] split = set.split(":");
                    int q1 = Integer.parseInt(split[0]);
                    int q2 = Integer.parseInt(split[1]);
                    int r = Integer.parseInt(split[2]);

                    if (tradeManagers.size() <= offset) {
                        ItemStack noItem = plugin.getNoItemItem();
                        inv.setItem(q1, noItem);
                        inv.setItem(q2, noItem);
                        inv.setItem(r, noItem);
                    } else {
                        ItemStack q1Item = tradeManagers.get(offset).getQuest1Item();
                        ItemStack q2Item = tradeManagers.get(offset).getQuest2Item();
                        ItemStack rItem = tradeManagers.get(offset).getRewardItem();

                        if (q1Item == null) {
                            q1Item = plugin.getNoItemItem();
                        }
                        if (q2Item == null) {
                            q2Item = plugin.getNoItemItem();
                        }


                        NBTItem q1NBT = new NBTItem(q1Item);
                        NBTItem q2NBT = new NBTItem(q2Item);
                        NBTItem rNBT = new NBTItem(rItem);

                        q1NBT.setInteger("NeoIndex", offset);
                        q2NBT.setInteger("NeoIndex", offset);
                        rNBT.setInteger("NeoIndex", offset);

                        q1NBT.applyNBT(q1Item);
                        q2NBT.applyNBT(q2Item);
                        rNBT.applyNBT(rItem);

                        inv.setItem(q1, q1Item);
                        inv.setItem(q2, q2Item);
                        inv.setItem(r, rItem);
                        offset++;
                    }
                } catch (IndexOutOfBoundsException ignored) {

                }
            }
        }
    }
    private Inventory createGUI() {
        List<String> layout = mainConfig.getStringList("GUI.Layout");
        int rows = layout.size();
        Inventory gui = TradeInventoryManager.createPluginInventory(rows * 9, plugin.colorize(config.getString("Option.Title", "Default Shop")), id);

        Objects.requireNonNull(mainConfig.getConfigurationSection("GUI.Items")).getKeys(false).forEach(key -> {
            String path = "GUI.Items." + key;
            String type = mainConfig.getString(path + ".Type", "VANILLA").toUpperCase();
            String item = mainConfig.getString(path + ".Item", "STONE");
            String display = mainConfig.getString(path + ".Display");
            List<String> lore = mainConfig.getStringList(path + ".Lore");
            int modelData = mainConfig.getInt(path + ".ModelData", 0);
            int amount = mainConfig.getInt(path + ".Amount", 1);

            ItemStack stack = createItemStack(type, item, display, lore, modelData, amount);

            for (int i = 0; i < rows; i++) {
                String row = layout.get(i);
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == key.charAt(0)) {
                        gui.setItem(i * 9 + j, stack);
                    }
                }
            }
        });

        return gui;
    }

    private ItemStack createItemStack(String type, String item, String display, List<String> lore, int modelData, int amount) {
        ItemStack stack = new ItemStack(Material.COBBLESTONE);
        if (type.equalsIgnoreCase("Vanilla")) {
            Material material = Material.getMaterial(item);
            if (material == null) material = Material.BARRIER;
            // Create a vanilla item stack using Bukkit API.
            ItemStack tempStack = new ItemStack(material, amount);
            stack = setItemMeta(display, lore, modelData, amount, stack, tempStack);
        } else if (type.equalsIgnoreCase("MI")) {
            String[] typeId = item.split(":");
            String mmoType = typeId[0];
            String id = typeId[1];
            ItemStack tempStack = MMOItems.plugin.getItem(mmoType, id);
            if (tempStack == null) {
                tempStack = new ItemStack(Material.STONE);
            }
            stack = setItemMeta(display, lore, modelData, amount, stack, tempStack);
        } else if (type.equalsIgnoreCase("IA")) {
            ItemStack tempStack;
            try {
                tempStack = CustomStack.getInstance(item).getItemStack();
            } catch (NullPointerException e) {
                tempStack = new ItemStack(Material.STONE);
            }
            stack = setItemMeta(display, lore, modelData, amount, stack, tempStack);
        } else {
            stack = new ItemStack(Material.STONE);
            stack.setAmount(amount);
        }
        NBTItem nbt = new NBTItem(stack);
        nbt.setString("NeoShopID", id);
        nbt.applyNBT(stack);
        return stack;
    }

    @NotNull
    private ItemStack setItemMeta(String display, List<String> lore, int modelData, int amount, ItemStack stack, ItemStack tempStack) {
        ItemMeta meta = tempStack.getItemMeta();
        if (meta != null) {
            if(display != null) meta.displayName(plugin.colorize(display));
            if(lore != null) {
                ArrayList<Component> lo = new ArrayList<>();
                for (String l : lore) {
                    lo.add(plugin.colorize(l));
                }
                meta.lore(lo);
            }
            if (modelData != 0) {
                meta.setCustomModelData(modelData);
            }
            tempStack.setItemMeta(meta);
        }
        stack.setAmount(amount);
        stack = tempStack;
        return stack;
    }

    public ArrayList<TradeManager> getTradeManagers() {
        return tradeManagers;
    }

    public void buildEditGUI(Player player) {
        Inventory inv = createEditGUI();
        player.openInventory(inv);
    }

    public Inventory createEditGUI() {
        Inventory inv = EditInventoryManager.createInventory(54, Component.text("Editing: " + id), id);
        inv.setItem(53, new ItemStack(Material.YELLOW_WOOL));
        inv.setItem(44, new ItemStack(Material.LIME_WOOL));
        inv.setItem(35, new ItemStack(Material.RED_WOOL));

        int i = 0;
        int rewardIndex = 0;
        int q1Index = 9;
        int q2Index = 18;
        for (TradeManager tradeSet : tradeManagers) {
            i++;
            if (i == 10) {
                rewardIndex += 18;
                q1Index += 18;
                q2Index += 18;
            }

            inv.setItem(rewardIndex, tradeSet.getRewardItem());
            inv.setItem(q1Index, tradeSet.getQuest1Item());
            inv.setItem(q2Index, tradeSet.getQuest2Item());

            rewardIndex++;
            q1Index++;
            q2Index++;
        }
        return inv;
    }
    public void saveEdit(Inventory inv) {
        config.set("Items", null);
        int index = 0;
        for (int i = 0; i < 16 ; i++) {
            if (index == 9) {
                index += 18;
            }

            int rewardSlot = index;
            int q1Slot = index+9;
            int q2Slot = index+18;

            if (inv.getItem(index) == null) {
                index++;
                continue;
            }
            ConfigurationSection section = config.createSection("Items."+i);
            ItemManager rewardItem = new ItemManager(inv.getItem(rewardSlot));
            ItemManager q1Item = new ItemManager(inv.getItem(q1Slot));
            ItemManager q2Item = new ItemManager(inv.getItem(q2Slot));

            setSection(section, "Reward", rewardItem);
            setSection(section, "Quests.1", q1Item);
            setSection(section, "Quests.2", q2Item);

            index++;
        }
        try {
            config.save(new File(plugin.getDataFolder(), "Shops/"+id+".yml"));
            plugin.reload();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void setSection(ConfigurationSection section, String path,ItemManager item) {
        section.set(path+".Type", item.getType());
        section.set(path+".Item", item.getStringItem());
        section.set(path+".Amount", item.getAmount());
    }
}
