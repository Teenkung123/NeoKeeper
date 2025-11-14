package org.teenkung.neokeeper.GUIs.station;

import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils.AggregatedMaterial;
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ItemComparison;
import org.teenkung.neokeeper.Utils.ItemTextFormatter;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds shared lore snippets for station GUIs so both the listing view and
 * player recipe view stay in sync with the same configuration.
 */
public final class StationLoreBuilder {

    private StationLoreBuilder() {}

    public static List<Component> buildPlayerRecipeLore(NeoKeeper plugin, StationRecipe recipe, Player player) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Player.List.RecipeLore");
        List<String> template = section != null ? section.getStringList("Lines") : List.of("&7Materials:", "%materials%");
        if (template.isEmpty()) {
            template = List.of("&7Materials:", "%materials%");
        }
        String entryTemplate = section != null ? section.getString("MaterialsEntry", "&f- &e%amount%x %item%") : "&f- &e%amount%x %item%";
        String emptyTemplate = section != null ? section.getString("MaterialsEmpty", "&8None") : "&8None";
        ConfigurationSection statusSection = section != null ? section.getConfigurationSection("StatusIndicator") : null;
        boolean statusEnabled = statusSection != null && statusSection.getBoolean("Enabled", false);
        String enoughPrefix = statusSection != null ? statusSection.getString("Enough", "&a+ ") : "&a+ ";
        String missingPrefix = statusSection != null ? statusSection.getString("Missing", "&c- ") : "&c- ";
        ConfigurationSection countsSection = statusSection != null ? statusSection.getConfigurationSection("Counts") : null;
        boolean countsEnabled = countsSection != null && countsSection.getBoolean("Enabled", false);
        String countsFormat = countsSection != null ? countsSection.getString("Format", "&7[%have%/%need%]") : "&7[%have%/%need%]";

        List<AggregatedMaterial> aggregated = StationMaterialUtils.aggregate(recipe.getMaterials());
        List<Component> materials = new ArrayList<>();
        for (AggregatedMaterial aggregatedMaterial : aggregated) {
            ItemManager manager = aggregatedMaterial.template();
            if (manager == null) {
                continue;
            }
            ItemStack stack = manager.getItem();
            String display = ItemTextFormatter.displayNameFormatted(stack);
            int totalRequired = aggregatedMaterial.amount();
            int have = (statusEnabled || countsEnabled) ? countPlayerItems(player, manager) : 0;
            boolean hasEnough = have >= totalRequired;
            String prefix = statusEnabled ? (hasEnough ? enoughPrefix : missingPrefix) : "";
            String suffix = countsEnabled ? countsFormat
                    .replace("%have%", String.valueOf(have))
                    .replace("%need%", String.valueOf(totalRequired)) : "";
            String entry = entryTemplate
                    .replace("%amount%", String.valueOf(totalRequired))
                    .replace("%item%", display)
                    .replace("%status_prefix%", prefix)
                    .replace("%status_suffix%", suffix);
            if (!entryTemplate.contains("%status_prefix%")) {
                entry = prefix + entry;
            }
            if (!entryTemplate.contains("%status_suffix%")) {
                entry = entry + suffix;
            }
            materials.add(plugin.colorize(entry));
        }

        List<Component> lore = new ArrayList<>();
        String recipeName = ItemTextFormatter.displayNameFormatted(recipe.getResultItem());
        for (String raw : template) {
            if ("%materials%".equalsIgnoreCase(raw.trim())) {
                if (materials.isEmpty()) {
                    lore.add(plugin.colorize(emptyTemplate));
                } else {
                    lore.addAll(materials);
                }
                continue;
            }
            lore.add(plugin.colorize(raw.replace("%recipe%", recipeName)));
        }
        return lore;
    }

    public static int countPlayerItems(Player player, ItemManager requirement) {
        if (player == null || requirement == null) {
            return 0;
        }
        PlayerInventory inventory = player.getInventory();
        int count = 0;
        for (ItemStack stack : inventory.getContents()) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            if (ItemComparison.matches(new ItemManager(stack), requirement)) {
                count += stack.getAmount();
            }
        }
        return count;
    }
}
