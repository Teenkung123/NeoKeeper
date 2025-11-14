package org.teenkung.neokeeper.Utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility helpers for resolving user-facing item names with proper coloring.
 */
public final class ItemTextFormatter {

    private ItemTextFormatter() {}

    public static Component displayName(ItemStack stack) {
        if (stack == null || stack.getType().isAir()) {
            return Component.text("Unknown Item");
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta != null && meta.hasDisplayName()) {
            Component custom = meta.displayName();
            if (custom != null) {
                return custom;
            }
        }

        Component display = stack.displayName();
        if (display != null) {
            return display;
        }

        Material type = stack.getType();
        if (type != null) {
            return Component.translatable(type.translationKey());
        }
        return Component.text("Unknown Item");
    }

    public static String displayNameFormatted(ItemStack stack) {
        Component component = displayName(stack);
        String legacy = LegacyComponentSerializer.legacyAmpersand().serialize(component);
        return convertHexColors(legacy);
    }

    private static final Pattern HEX_PATTERN = Pattern.compile("(?i)&x(?:&[0-9a-f]){6}");

    private static String convertHexColors(String input) {
        Matcher matcher = HEX_PATTERN.matcher(input);
        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String match = matcher.group();
            String hex = match.replace("&x", "").replace("&", "");
            matcher.appendReplacement(buffer, "<#" + hex + ">");
        }
        matcher.appendTail(buffer);
        return buffer.toString();
    }
}
