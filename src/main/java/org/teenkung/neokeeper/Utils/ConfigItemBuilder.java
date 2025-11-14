package org.teenkung.neokeeper.Utils;

import dev.lone.itemsadder.api.CustomStack;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ExcellentCratesHook;

import java.util.ArrayList;
import java.util.List;

public final class ConfigItemBuilder {

    private ConfigItemBuilder() {}

    public static ItemStack fromSection(NeoKeeper plugin, ConfigurationSection section) {
        if (section == null) {
            return new ItemStack(Material.BARRIER);
        }
        String type = section.getString("Type", "VANILLA").toUpperCase();
        String item = section.getString("Item", "STONE");
        String display = section.getString("Display");
        List<String> lore = section.getStringList("Lore");
        int modelData = section.getInt("ModelData", 0);
        int amount = section.getInt("Amount", 1);

        ItemStack stack = switch (type) {
            case "VANILLA" -> {
                Material material = Material.matchMaterial(item);
                if (material == null) {
                    plugin.getLogger().warning("Unknown material " + item + " in config.");
                    material = Material.BARRIER;
                }
                yield new ItemStack(material, amount);
            }
            case "MI" -> {
                String[] parts = item.split(":");
                if (parts.length < 2) {
                    plugin.getLogger().warning("Invalid MMOItems identifier " + item);
                    yield new ItemStack(Material.BARRIER);
                }
                ItemStack mmoStack = MMOItems.plugin.getItem(parts[0], parts[1]);
                if (mmoStack == null) {
                    plugin.getLogger().warning("Unknown MMOItem " + item);
                    yield new ItemStack(Material.BARRIER);
                }
                mmoStack.setAmount(amount);
                yield mmoStack;
            }
            case "EC", "EC_KEY" -> {
                ItemStack keyStack = ExcellentCratesHook.getKeyItem(item);
                if (keyStack == null) {
                    plugin.getLogger().warning("Unknown ExcellentCrates key " + item);
                    yield new ItemStack(Material.BARRIER);
                }
                keyStack.setAmount(amount);
                yield keyStack;
            }
            case "IA" -> {
                CustomStack customStack = CustomStack.getInstance(item);
                if (customStack == null) {
                    plugin.getLogger().warning("Unknown ItemsAdder item " + item);
                    yield new ItemStack(Material.BARRIER);
                }
                ItemStack iaStack = customStack.getItemStack();
                iaStack.setAmount(amount);
                yield iaStack;
            }
            default -> {
                plugin.getLogger().warning("Unknown item type " + type + " in config.");
                yield new ItemStack(Material.BARRIER);
            }
        };

        ItemMeta meta = stack.getItemMeta();
        if (meta != null) {
            if (display != null && !display.isBlank()) {
                meta.displayName(plugin.colorize(display));
            }
            if (lore != null && !lore.isEmpty()) {
                List<Component> loreComponents = new ArrayList<>();
                for (String line : lore) {
                    loreComponents.add(plugin.colorize(line));
                }
                meta.lore(loreComponents);
            }
            if (modelData > 0) {
                meta.setCustomModelData(modelData);
            }
            stack.setItemMeta(meta);
        }
        return stack;
    }
}
