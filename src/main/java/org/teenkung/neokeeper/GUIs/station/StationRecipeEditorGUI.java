package org.teenkung.neokeeper.GUIs.station;

import net.kyori.adventure.text.Component;
import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.Material;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ConfigItemBuilder;
import org.teenkung.neokeeper.Utils.ItemTextFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StationRecipeEditorGUI {

    private final NeoKeeper plugin;
    private final StationDefinition station;
    private final List<Integer> materialSlots;
    private final int resultSlot;

    public StationRecipeEditorGUI(NeoKeeper plugin, StationDefinition station) {
        this.plugin = plugin;
        this.station = station;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Editor.Recipe");
        this.materialSlots = resolveSlots(section, "MaterialSlots", StationGUIConstants.MATERIAL_SLOTS);
        this.resultSlot = section != null ? section.getInt("ResultSlot", StationGUIConstants.RESULT_SLOT) : StationGUIConstants.RESULT_SLOT;
    }

    public void open(Player player, String recipeKey, int previousPage) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Editor.Recipe");
        if (section == null) {
            player.sendMessage(plugin.colorize("<red>Station recipe editor is not configured."));
            return;
        }
        int size = sanitizeSize(section.getInt("Size", 54));
        StationRecipe recipe = station.getRecipe(recipeKey);
        String recipeName = recipe != null ? ItemTextFormatter.displayNameFormatted(recipe.getResultItem()) : recipeKey;

        String titleRaw = section.getString("Title", "&eEditing Recipe: %recipe%");
        Component title = plugin.colorize(titleRaw
                .replace("%id%", station.getId())
                .replace("%title%", station.getPlainTitle())
                .replace("%recipe%", recipeName));

        Inventory inv = Bukkit.createInventory(new EditorRecipeHolder(station.getId(), recipeKey, previousPage), size, title);
        ItemStack filler = ConfigItemBuilder.fromSection(plugin, section.getConfigurationSection("Items.Background"));
        fill(inv, filler);
        clearEditableSlots(inv);

        if (recipe != null) {
            populateRecipe(inv, recipe);
        }

        placeButton(inv, section.getConfigurationSection("Items.Back"));
        placeButton(inv, section.getConfigurationSection("Items.Save"));

        player.openInventory(inv);
    }

    public boolean save(Player player, Inventory inv, String recipeKey) {
        ItemStack result = inv.getItem(resultSlot);
        if (result == null || result.getType().isAir()) {
            player.sendMessage(plugin.colorize("<red>Result item cannot be empty."));
            return false;
        }
        List<ItemStack> materials = new ArrayList<>();
        for (int slot : materialSlots) {
            ItemStack stack = inv.getItem(slot);
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            materials.add(stack.clone());
        }
        if (materials.isEmpty()) {
            player.sendMessage(plugin.colorize("<red>Add at least one material."));
            return false;
        }

        YamlConfiguration config = station.getConfig();
        ConfigurationSection recipesSection = config.getConfigurationSection("Recipes");
        if (recipesSection == null) {
            recipesSection = config.createSection("Recipes");
        }
        ConfigurationSection recipeSection = recipesSection.getConfigurationSection(recipeKey);
        if (recipeSection == null) {
            recipeSection = recipesSection.createSection(recipeKey);
        }
        recipeSection.set("Result", null);
        recipeSection.set("Materials", null);

        ItemManager resultManager = new ItemManager(result);
        ConfigurationSection resultSection = recipeSection.createSection("Result");
        writeItem(resultSection, resultManager);

        ConfigurationSection materialsSection = recipeSection.createSection("Materials");
        int index = 0;
        for (ItemStack stack : materials) {
            ItemManager manager = new ItemManager(stack);
            ConfigurationSection materialSection = materialsSection.createSection(String.valueOf(index++));
            writeItem(materialSection, manager);
        }

        station.saveConfig();
        station.reload();
        player.sendMessage(plugin.colorize("<green>Recipe saved."));
        return true;
    }

    private void populateRecipe(Inventory inv, StationRecipe recipe) {
        List<ItemStack> materials = new ArrayList<>();
        List<ItemManager> sortedMaterials = StationMaterialUtils.sortByAmountDesc(recipe.getMaterials());
        for (ItemManager manager : sortedMaterials) {
            ItemStack stack = manager.getItem();
            if (stack == null) {
                continue;
            }
            ItemStack clone = stack.clone();
            clone.setAmount(manager.getAmount());
            materials.add(clone);
        }
        for (int i = 0; i < materials.size() && i < materialSlots.size(); i++) {
            int slot = materialSlots.get(i);
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, materials.get(i));
            }
        }
        if (resultSlot < inv.getSize()) {
            ItemStack result = safeClone(recipe.getResultItem(), true);
            inv.setItem(resultSlot, result);
        }
    }

    private void fill(Inventory inv, ItemStack filler) {
        if (filler == null) {
            return;
        }
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, filler.clone());
        }
    }

    private void placeButton(Inventory inv, ConfigurationSection section) {
        if (section == null) {
            return;
        }
        ItemStack item = decorateButton(ConfigItemBuilder.fromSection(plugin, section), section.getString("Action"));
        placeIntoSlots(inv, section, item);
    }

    private void placeIntoSlots(Inventory inv, ConfigurationSection section, ItemStack item) {
        if (section == null || item == null) {
            return;
        }
        List<Integer> slots = new ArrayList<>();
        if (section.isList("Slots")) {
            slots.addAll(section.getIntegerList("Slots"));
        }
        if (section.isSet("Slot")) {
            slots.add(section.getInt("Slot"));
        }
        if (slots.isEmpty()) {
            return;
        }
        for (Integer slot : slots) {
            if (slot == null) continue;
            if (slot < 0 || slot >= inv.getSize()) continue;
            inv.setItem(slot, item.clone());
        }
    }

    private ItemStack decorateButton(ItemStack item, String action) {
        if (item == null || action == null || action.isBlank()) {
            return item;
        }
        ItemStack clone = safeClone(item, false);
        NBT.modify(clone, (Consumer<ReadWriteItemNBT>) nbt -> nbt.setString("NeoStationAction", action.toUpperCase()));
        return clone;
    }

    private void writeItem(ConfigurationSection section, ItemManager manager) {
        section.set("Type", manager.getType());
        section.set("Item", manager.getStringItem());
        section.set("Amount", manager.getAmount());
    }

    private int sanitizeSize(int configured) {
        int size = ((configured + 8) / 9) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        return size;
    }

    private void clearEditableSlots(Inventory inv) {
        for (int slot : materialSlots) {
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, null);
            }
        }
        if (resultSlot >= 0 && resultSlot < inv.getSize()) {
            inv.setItem(resultSlot, null);
        }
    }

    private List<Integer> resolveSlots(ConfigurationSection section, String key, List<Integer> fallback) {
        if (section == null) {
            return fallback;
        }
        List<Integer> slots = section.getIntegerList(key);
        if (slots == null || slots.isEmpty()) {
            return fallback;
        }
        return List.copyOf(slots);
    }

    public List<Integer> getMaterialSlots() {
        return materialSlots;
    }

    public int getResultSlot() {
        return resultSlot;
    }

    private ItemStack safeClone(ItemStack source, boolean allowNoItem) {
        ItemStack clone = source;
        if (clone == null && allowNoItem) {
            clone = plugin.getNoItemItem();
        }
        clone = clone != null ? clone.clone() : null;
        if (clone == null || clone.getType().isAir() || clone.getAmount() <= 0) {
            clone = new ItemStack(Material.AIR);
        }
        return clone;
    }

    public static class EditorRecipeHolder implements InventoryHolder {

        private final String stationId;
        private final String recipeKey;
        private final int previousPage;

        public EditorRecipeHolder(String stationId, String recipeKey, int previousPage) {
            this.stationId = stationId;
            this.recipeKey = recipeKey;
            this.previousPage = previousPage;
        }

        public String getStationId() {
            return stationId;
        }

        public String getRecipeKey() {
            return recipeKey;
        }

        public int getPreviousPage() {
            return previousPage;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
