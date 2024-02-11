package org.teenkung.neokeeper;

import de.tr7zw.nbtapi.NBTItem;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.teenkung.neokeeper.Commands.CommandTabComplete;
import org.teenkung.neokeeper.Commands.CommandsHandler;
import org.teenkung.neokeeper.Handlers.EditGUIHandler;
import org.teenkung.neokeeper.Handlers.TradeGUIHandler;
import org.teenkung.neokeeper.Managers.Edit.EditInventoryManager;
import org.teenkung.neokeeper.Managers.InventoriesLoader;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;

public final class NeoKeeper extends JavaPlugin {

    private InventoriesLoader shopLoader;
    private ConfigLoader configLoader;
    @Override
    public void onEnable() {
        this.configLoader = new ConfigLoader(this);
        this.shopLoader = new InventoriesLoader(this);
        shopLoader.loadAllShop();

        Bukkit.getPluginManager().registerEvents(new TradeGUIHandler(this), this);
        Bukkit.getPluginManager().registerEvents(new EditGUIHandler(this), this);

        PluginCommand cmd = getCommand("neokeeper");
        if (cmd != null) {
            cmd.setExecutor(new CommandsHandler(this));
            cmd.setTabCompleter(new CommandTabComplete(this));
        } else {
            getLogger().severe("Could not register the plugin commands! Disabling Plugin. . .");
            Bukkit.getPluginManager().disablePlugin(this);
        }
    }

    @Override
    public void onDisable() {
        for (Inventory inv : EditInventoryManager.getAllInventories().keySet()) {
            for (HumanEntity entity : inv.getViewers()) {
                entity.closeInventory();
            }
        }

        for (Inventory inv : TradeInventoryManager.getAllInventories().keySet()) {
            for (HumanEntity entity : inv.getViewers()) {
                entity.closeInventory();
            }
        }
    }

    public void reload() {
        this.configLoader = new ConfigLoader(this);
        this.shopLoader = new InventoriesLoader(this);
        shopLoader.loadAllShop();
    }

    public InventoriesLoader getShopLoader() { return shopLoader; }
    public ConfigLoader getConfigLoader() { return configLoader; }

    /**
     * Transforms Minecraft color codes in a string to mini messages color format.
     * @param message The string containing Minecraft color codes.
     * @return A string with mini messages color format.
     */
    public Component colorize(String message) {
        if (message == null) return Component.text("");
        String result;
        result = message.replaceAll("&0", "<black>");
        result = result.replaceAll("&1", "<dark_blue>");
        result = result.replaceAll("&2", "<dark_green>");
        result = result.replaceAll("&3", "<dark_aqua>");
        result = result.replaceAll("&4", "<dark_red>");
        result = result.replaceAll("&5", "<dark_purple>");
        result = result.replaceAll("&6", "<gold>");
        result = result.replaceAll("&7", "<gray>");
        result = result.replaceAll("&8", "<dark_gray>");
        result = result.replaceAll("&9", "<blue>");
        result = result.replaceAll("&a", "<green>");
        result = result.replaceAll("&b", "<aqua>");
        result = result.replaceAll("&c", "<red>");
        result = result.replaceAll("&d", "<light_purple>");
        result = result.replaceAll("&e", "<yellow>");
        result = result.replaceAll("&f", "<white>");

        // Formatting codes
        result = result.replaceAll("&k", "<obfuscated>");
        result = result.replaceAll("&l", "<bold>");
        result = result.replaceAll("&m", "<strikethrough>");
        result = result.replaceAll("&n", "<underline>");
        result = result.replaceAll("&o", "<italic>");
        result = result.replaceAll("&r", "<reset>");

        return MiniMessage.miniMessage().deserialize(result);
    }

    public ItemStack getNoItemItem() {
        ItemStack filItem = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = filItem.getItemMeta();
        meta.displayName(Component.text(""));
        filItem.setItemMeta(meta);
        NBTItem nbt = new NBTItem(filItem);
        nbt.setBoolean("NeoShopID", true);
        return nbt.getItem();
    }
}
