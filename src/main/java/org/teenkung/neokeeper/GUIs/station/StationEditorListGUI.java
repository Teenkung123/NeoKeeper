package org.teenkung.neokeeper.GUIs.station;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadWriteItemNBT;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.Material;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils.AggregatedMaterial;
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ConfigItemBuilder;
import org.teenkung.neokeeper.Utils.ItemTextFormatter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StationEditorListGUI {

    private static final List<Integer> DEFAULT_RECIPE_SLOTS = Arrays.asList(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    );

    private final NeoKeeper plugin;
    private final StationDefinition station;

    public StationEditorListGUI(NeoKeeper plugin, StationDefinition station) {
        this.plugin = plugin;
        this.station = station;
    }

    public void open(Player player, int page) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Editor.List");
        if (section == null) {
            player.sendMessage(plugin.colorize("<red>Station editor list is not configured."));
            return;
        }

        int size = sanitizeSize(section.getInt("Size", 54));
        String titleRaw = section.getString("Title", "&eEditing: %id%");
        Component title = plugin.colorize(titleRaw
                .replace("%id%", station.getId())
                .replace("%title%", station.getPlainTitle()));

        Inventory inv = Bukkit.createInventory(new EditorListHolder(station.getId(), Math.max(page, 0)), size, title);
        ItemStack filler = ConfigItemBuilder.fromSection(plugin, section.getConfigurationSection("Items.Background"));
        fill(inv, filler);

        List<Integer> recipeSlots = section.getIntegerList("RecipeSlots");
        if (recipeSlots.isEmpty()) {
            recipeSlots = DEFAULT_RECIPE_SLOTS;
        }
        int perPage = recipeSlots.size();
        int maxPage = Math.max(0, (int) Math.ceil((double) station.getRecipes().size() / perPage) - 1);
        page = Math.max(0, Math.min(page, maxPage));
        ((EditorListHolder) inv.getHolder()).setPage(page);

        int startIndex = page * perPage;
        List<StationRecipe> recipes = station.getRecipes();
        for (int i = 0; i < recipeSlots.size(); i++) {
            int slot = recipeSlots.get(i);
            int recipeIndex = startIndex + i;
            if (recipeIndex >= recipes.size() || slot >= inv.getSize()) {
                continue;
            }
            StationRecipe recipe = recipes.get(recipeIndex);
            ItemStack display = buildDisplay(recipe);
            inv.setItem(slot, display);
        }

        placeButton(inv, section.getConfigurationSection("Items.Add"));
        placeButton(inv, section.getConfigurationSection("Items.Back"));
        placeButton(inv, section.getConfigurationSection("Items.Delete"));
        if (page < maxPage) {
            placeButton(inv, section.getConfigurationSection("Items.Next"));
        }
        if (page > 0) {
            placeButton(inv, section.getConfigurationSection("Items.Previous"));
        }

        player.openInventory(inv);
    }

    private ItemStack buildDisplay(StationRecipe recipe) {
        ItemStack base = safeClone(recipe.getResultItem(), true);
        ItemMeta meta = base.getItemMeta();
        List<Component> lore = buildLore(recipe);
        if (meta != null) {
            meta.lore(lore);
            base.setItemMeta(meta);
        }
        if (base.getType() == Material.AIR) return base;
        NBT.modify(base, (Consumer<ReadWriteItemNBT>) nbt -> {
            nbt.setString("NeoStationId", station.getId());
            nbt.setString("NeoStationRecipe", recipe.getKey());
            nbt.setString("NeoStationView", "EDITOR_LIST");
        });
        return base;
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

    private ItemStack safeClone(ItemStack source, boolean allowNoItem) {
        ItemStack clone = source;
        if (clone == null && allowNoItem) {
            clone = plugin.getNoItemItem();
        }
        clone = clone != null ? clone.clone() : null;
        if (clone == null || clone.getType().isAir() || clone.getAmount() <= 0) {
            clone = new ItemStack(Material.BARRIER);
        }
        return clone;
    }

    private int sanitizeSize(int configured) {
        int size = ((configured + 8) / 9) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        return size;
    }

    private List<Component> buildLore(StationRecipe recipe) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Editor.List.RecipeLore");
        List<String> template = section != null ? section.getStringList("Lines") : List.of(
                "&7Left-click: edit recipe",
                "&7Shift-right-click: delete",
                "&f",
                "&fMaterials:",
                "%materials%"
        );
        if (template.isEmpty()) {
            template = List.of("&7Left-click: edit recipe", "&7Shift-right-click: delete", "&fMaterials:", "%materials%");
        }
        String entryTemplate = section != null ? section.getString("MaterialsEntry", "&e- %amount%x %item%") : "&e- %amount%x %item%";
        String emptyTemplate = section != null ? section.getString("MaterialsEmpty", "&8None") : "&8None";

        List<Component> materials = new ArrayList<>();
        List<AggregatedMaterial> aggregated = StationMaterialUtils.aggregate(recipe.getMaterials());
        for (AggregatedMaterial aggregatedMaterial : aggregated) {
            if (aggregatedMaterial == null || aggregatedMaterial.template() == null) {
                continue;
            }
            String display = ItemTextFormatter.displayNameFormatted(aggregatedMaterial.template().getItem());
            String entry = entryTemplate
                    .replace("%amount%", String.valueOf(aggregatedMaterial.amount()))
                    .replace("%item%", display);
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

    public int getTotalPages() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Editor.List");
        if (section == null) {
            return 1;
        }
        List<Integer> recipeSlots = section.getIntegerList("RecipeSlots");
        if (recipeSlots.isEmpty()) {
            recipeSlots = DEFAULT_RECIPE_SLOTS;
        }
        int perPage = recipeSlots.size();
        return Math.max(1, (int) Math.ceil((double) station.getRecipes().size() / perPage));
    }

    public static class EditorListHolder implements InventoryHolder {

        private final String stationId;
        private int page;

        public EditorListHolder(String stationId, int page) {
            this.stationId = stationId;
            this.page = page;
        }

        public String getStationId() {
            return stationId;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
