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
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils.AggregatedMaterial;
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ConfigItemBuilder;
import org.teenkung.neokeeper.Utils.ItemTextFormatter;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class StationPlayerRecipeGUI {

    private final NeoKeeper plugin;
    private final StationDefinition station;
    private final List<Integer> materialSlots;
    private final int resultSlot;
    private final String materialCounterFormat;

    public StationPlayerRecipeGUI(NeoKeeper plugin, StationDefinition station) {
        this.plugin = plugin;
        this.station = station;
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Player.Recipe");
        this.materialSlots = resolveSlots(section, "MaterialSlots", StationGUIConstants.MATERIAL_SLOTS);
        this.resultSlot = section != null ? section.getInt("ResultSlot", StationGUIConstants.RESULT_SLOT) : StationGUIConstants.RESULT_SLOT;
        String counter = section != null ? section.getString("MaterialNameSuffix", "&7[%have%/%need%]") : "&7[%have%/%need%]";
        this.materialCounterFormat = counter != null && !counter.isBlank() ? counter : null;
    }

    public void open(Player player, StationRecipe recipe, int previousPage) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Player.Recipe");
        if (section == null) {
            player.sendMessage(plugin.colorize("<red>Station recipe GUI is not configured."));
            return;
        }

        int size = sanitizeSize(section.getInt("Size", 45));
        String recipeName = ItemTextFormatter.displayNameFormatted(recipe.getResultItem());
        String titleRaw = section.getString("Title", "&aCrafting: %recipe%");
        Component title = plugin.colorize(titleRaw
                .replace("%id%", station.getId())
                .replace("%title%", station.getPlainTitle())
                .replace("%recipe%", recipeName));

        Inventory inv = Bukkit.createInventory(new PlayerRecipeHolder(station.getId(), recipe.getKey(), previousPage), size, title);
        ItemStack filler = ConfigItemBuilder.fromSection(plugin, section.getConfigurationSection("Items.Background"));
        fill(inv, filler);

        List<ItemManager> materials = StationMaterialUtils.sortByAmountDesc(recipe.getMaterials());
        List<AggregatedMaterial> aggregatedMaterials = StationMaterialUtils.aggregate(materials);
        int[] remainingHave = new int[aggregatedMaterials.size()];
        for (int i = 0; i < aggregatedMaterials.size(); i++) {
            remainingHave[i] = StationLoreBuilder.countPlayerItems(player, aggregatedMaterials.get(i).template());
        }

        for (int i = 0; i < materials.size() && i < materialSlots.size(); i++) {
            ItemManager material = materials.get(i);
            ItemStack stack = material.getItem();
            if (stack == null) {
                continue;
            }
            ItemStack clone = stack.clone();
            clone.setAmount(material.getAmount());
            int haveForStack = 0;
            int aggregationIndex = StationMaterialUtils.findAggregationIndex(aggregatedMaterials, material);
            if (aggregationIndex >= 0 && aggregationIndex < remainingHave.length) {
                int available = remainingHave[aggregationIndex];
                haveForStack = Math.min(material.getAmount(), available);
                remainingHave[aggregationIndex] = Math.max(0, available - haveForStack);
            }
            decorateMaterialItem(clone, material, haveForStack);
            int slot = materialSlots.get(i);
            if (slot >= 0 && slot < inv.getSize()) {
                inv.setItem(slot, clone);
            }
        }

        ItemStack result = safeClone(recipe.getResultItem(), true);
        ItemMeta meta = result.getItemMeta();
        List<Component> resultLore = new ArrayList<>();
        if (meta != null && meta.hasLore() && meta.lore() != null) {
            resultLore.addAll(meta.lore());
        }
        List<Component> materialsLore = StationLoreBuilder.buildPlayerRecipeLore(plugin, recipe, player);
        if (!materialsLore.isEmpty()) {
            if (!resultLore.isEmpty()) {
                resultLore.add(Component.empty());
            }
            resultLore.addAll(materialsLore);
        }
        resultLore.add(plugin.colorize("&7Click to craft"));
        if (meta == null) {
            meta = result.getItemMeta();
        }
        meta.lore(resultLore);
        result.setItemMeta(meta);
        if (result.getType() != Material.AIR) {
            NBT.modify(result, (Consumer<ReadWriteItemNBT>) nbt -> {
                nbt.setString("NeoStationId", station.getId());
                nbt.setString("NeoStationRecipe", recipe.getKey());
                nbt.setString("NeoStationView", "PLAYER_DETAIL");
            });
        }
        if (resultSlot < inv.getSize()) {
            inv.setItem(resultSlot, result);
        }

        placeButton(inv, section.getConfigurationSection("Items.Back"));

        player.openInventory(inv);
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
        ArrayList<Integer> slots = new ArrayList<>();
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
            clone = new ItemStack(Material.AIR);
        }
        return clone;
    }

    private int sanitizeSize(int configured) {
        int size = ((configured + 8) / 9) * 9;
        if (size < 9) size = 9;
        if (size > 54) size = 54;
        return size;
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

    public int getResultSlot() {
        return resultSlot;
    }

    public List<Integer> getMaterialSlots() {
        return materialSlots;
    }

    private void decorateMaterialItem(ItemStack stack, ItemManager material, int haveForStack) {
        if (stack == null || material == null || materialCounterFormat == null) {
            return;
        }
        int need = Math.max(0, material.getAmount());
        int have = Math.max(0, Math.min(haveForStack, need));
        String rendered = materialCounterFormat
                .replace("%have%", String.valueOf(have))
                .replace("%need%", String.valueOf(need));
        ItemMeta meta = stack.getItemMeta();
        Component name = (meta != null && meta.displayName() != null)
                ? meta.displayName()
                : ItemTextFormatter.displayName(stack);
        Component decorated = name.append(Component.text(" ")).append(plugin.colorize(rendered));
        if (meta == null) {
            meta = stack.getItemMeta();
        }
        meta.displayName(decorated);
        stack.setItemMeta(meta);
    }

    public static class PlayerRecipeHolder implements InventoryHolder {

        private final String stationId;
        private final String recipeKey;
        private final int previousPage;

        public PlayerRecipeHolder(String stationId, String recipeKey, int previousPage) {
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
