package org.teenkung.neokeeper.Managers;

import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import org.bukkit.Bukkit;
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
import org.teenkung.neokeeper.NeoKeeper;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ShopManager {

    private final YamlConfiguration config;
    FileConfiguration mainConfig;
    private final NeoKeeper plugin;
    private final String id;
    private final ArrayList<TradeManager> tradeManagers;
    private final List<String> listPerPage;
    public ShopManager(NeoKeeper plugin, YamlConfiguration config, String id) {
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

    public void buildGUI(Player player) {
        Inventory inventory = createGUI();
        inventory.setItem(mainConfig.getInt("GUI.Slot.Quest1"), null);
        inventory.setItem(mainConfig.getInt("GUI.Slot.Quest2"), null);
        inventory.setItem(mainConfig.getInt("GUI.Slot.Reward"), plugin.getNoItemItem(id));
        player.openInventory(inventory);
        fillSelector(player, 0);
    }

    public void fillSelector(Player player, Integer offset) {
        TextComponent title = (TextComponent) player.getOpenInventory().title();
        if (title.content().equalsIgnoreCase(config.getString("Option.Title", "Default Shop"))) {
            InventoryView inv = player.getOpenInventory();

            for (String set : listPerPage) {
                String[] split = set.split(":");
                int q1 = Integer.parseInt(split[0]);
                int q2 = Integer.parseInt(split[1]);
                int r = Integer.parseInt(split[2]);

                if (tradeManagers.size() <= offset) {
                    ItemStack noItem = plugin.getNoItemItem(id);
                    inv.setItem(q1, noItem);
                    inv.setItem(q2, noItem);
                    inv.setItem(r, noItem);
                } else {
                    ItemStack q1Item = tradeManagers.get(offset).getQuest1Item();
                    ItemStack q2Item = tradeManagers.get(offset).getQuest2Item();
                    ItemStack rItem = tradeManagers.get(offset).getRewardItem();

                    if (q1Item == null) { q1Item = plugin.getNoItemItem(id); }
                    if (q2Item == null) { q2Item = plugin.getNoItemItem(id); }


                    NBTItem q1NBT = new NBTItem(q1Item);
                    NBTItem q2NBT = new NBTItem(q2Item);
                    NBTItem rNBT = new NBTItem(rItem);

                    q1NBT.setString("NeoShopID", id);
                    q1NBT.setInteger("NeoIndex", offset);
                    q2NBT.setString("NeoShopID", id);
                    q2NBT.setInteger("NeoIndex", offset);
                    rNBT.setString("NeoShopID", id);
                    rNBT.setInteger("NeoIndex", offset);

                    q1NBT.applyNBT(q1Item);
                    q2NBT.applyNBT(q2Item);
                    rNBT.applyNBT(rItem);

                    inv.setItem(q1, q1Item);
                    inv.setItem(q2, q2Item);
                    inv.setItem(r, rItem);
                    offset++;
                }

            }
        }
    }
    private Inventory createGUI() {
        List<String> layout = mainConfig.getStringList("GUI.Layout");
        int rows = layout.size();
        Inventory gui = InventoryManager.createPluginInventory(rows * 9, plugin.colorize(config.getString("Option.Title", "Default Shop")));

        Objects.requireNonNull(mainConfig.getConfigurationSection("GUI.Items")).getKeys(false).forEach(key -> {
            String path = "GUI.Items." + key;
            String type = mainConfig.getString(path + ".Type", "VANILLA").toUpperCase();
            String item = mainConfig.getString(path + ".Item", "STONE");
            String display = mainConfig.getString(path + ".Display");
            List<String> lore = mainConfig.getStringList(path + ".Lore");
            int modelData = mainConfig.getInt(path + ".ModelData", 0);
            int amount = mainConfig.getInt(path + ".Amount", 1);

            ItemStack stack = createItemStack(type, item, display, lore, modelData, amount);

            List<Integer> nextPage = mainConfig.getIntegerList("GUI.Slot.NextPage");
            List<Integer> previousPage = mainConfig.getIntegerList("GUI.Slot.PreviousPage");

            int slot = 0;
            for (int i = 0; i < rows; i++) {
                String row = layout.get(i);
                for (int j = 0; j < row.length(); j++) {
                    if (row.charAt(j) == key.charAt(0)) {
                        if (nextPage.contains(slot)) {
                            NBTItem nbt = new NBTItem(stack);
                            nbt.setBoolean("NeoNextPage", true);
                            nbt.setInteger("NeoOffset", 0);
                            nbt.applyNBT(stack);
                        } else if (previousPage.contains(slot)) {
                            NBTItem nbt = new NBTItem(stack);
                            nbt.setBoolean("NeoPreviousPage", true);
                            nbt.setInteger("NeoOffset", 0);
                            nbt.applyNBT(stack);
                        }
                        gui.setItem(i * 9 + j, stack);
                    }
                    slot++;
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
}
