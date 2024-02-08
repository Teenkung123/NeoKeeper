package org.teenkung.neokeeper;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.plugin.java.JavaPlugin;

public final class NeoKeeper extends JavaPlugin {

    private ShopLoader shopLoader;
    @Override
    public void onEnable() {
        this.shopLoader = new ShopLoader(this);
        shopLoader.loadAllShop();

        getCommand("neokeeper").setExecutor(new Commands(this));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public ShopLoader getShopLoader() { return shopLoader; }

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
}
