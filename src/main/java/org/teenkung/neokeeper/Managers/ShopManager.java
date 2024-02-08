package org.teenkung.neokeeper.Managers;

import de.tr7zw.nbtapi.NBTItem;
import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ShopManager {

    private final YamlConfiguration config;
    private final NeoKeeper plugin;
    private final String id;
    private final ArrayList<TradeManager> tradeManagers;
    public ShopManager(NeoKeeper plugin, YamlConfiguration config) {
        this.config = config;
        this.plugin = plugin;
        this.tradeManagers = new ArrayList<>();
        this.id = config.getName().replace(".yml", "").replace(".yaml", "");

        ConfigurationSection section = config.getConfigurationSection("Items");
        for (String key : section.getKeys(false)) {
            tradeManagers.add(new TradeManager(section.getConfigurationSection(key)));
        }
    }

    public void buildGUI(Player player) {
        Inventory inventory = createGUI();
        player.openInventory(inventory);
    }

    private Inventory createGUI() {
        FileConfiguration mainConfig = plugin.getConfig();

        List<String> layout = mainConfig.getStringList("GUI.Layout");
        int rows = layout.size();
        Inventory gui = Bukkit.createInventory(null, rows * 9, config.getString("Option.Title", "Default Shop"));

        mainConfig.getConfigurationSection("GUI.Items").getKeys(false).forEach(key -> {
            String path = "GUI.Items." + key;
            String type = mainConfig.getString(path + ".Type", "VANILLA").toUpperCase();
            String item = mainConfig.getString(path + ".Item", "STONE");
            String display = mainConfig.getString(path + ".Display", "Invalid Item");
            List<String> lore = mainConfig.getStringList(path + ".Lore");
            int modelData = mainConfig.getInt(path + ".ModelData", 0);
            int amount = mainConfig.getInt(path + ".Amount", 1);

            Bukkit.broadcastMessage("Type = " + type + ", Item = " + item);
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
            ItemStack vanillaStack = new ItemStack(material, amount);
            ItemMeta meta = vanillaStack.getItemMeta();
            if (meta != null) {
                meta.displayName(plugin.colorize(display));
                ArrayList<Component> lo = new ArrayList<>();
                for (String l : lore) {
                    lo.add(plugin.colorize(l));
                }
                meta.lore(lo);
                meta.setCustomModelData(modelData);
                vanillaStack.setItemMeta(meta);
            }
            stack = vanillaStack;
        } else if (type.equalsIgnoreCase("MI")) {
            String[] typeId = item.split(":");
            String mmoType = typeId[0];
            String id = typeId[1];
            stack = MMOItems.plugin.getItem(mmoType, id);
            if (stack == null) {
                stack = new ItemStack(Material.STONE);
            }
            stack.setAmount(amount);
        } else if (type.equalsIgnoreCase("IA")) {
            stack = CustomStack.getInstance("type").getItemStack();
            if (stack == null) {
                stack = new ItemStack(Material.STONE);
            }
            stack.setAmount(amount);
        } else {
            stack = new ItemStack(Material.STONE);
            stack.setAmount(amount);
        }
        NBTItem nbt = new NBTItem(stack);
        nbt.setString("NeoShopID", id);
        return nbt.getItem();
    }

}
