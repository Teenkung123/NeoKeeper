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
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ConfigItemBuilder;

import java.util.ArrayList;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

public class StationPlayerListGUI {

    private static final List<Integer> DEFAULT_RECIPE_SLOTS = Arrays.asList(
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    );

    private final NeoKeeper plugin;
    private final StationDefinition station;

    public StationPlayerListGUI(NeoKeeper plugin, StationDefinition station) {
        this.plugin = plugin;
        this.station = station;
    }

    public void open(Player player, int page) {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("Station.Player.List");
        if (section == null) {
            player.sendMessage(plugin.colorize("<red>Station list GUI is not configured."));
            return;
        }

        int size = sanitizeSize(section.getInt("Size", 54));
        String titleRaw = section.getString("Title", "&aStation: %id%");
        Component title = plugin.colorize(titleRaw
                .replace("%id%", station.getId())
                .replace("%title%", station.getPlainTitle()));

        Inventory inv = Bukkit.createInventory(new PlayerListHolder(station.getId(), Math.max(page, 0)), size, title);

        ItemStack filler = ConfigItemBuilder.fromSection(plugin, section.getConfigurationSection("Items.Background"));
        fill(inv, filler);

        List<Integer> recipeSlots = section.getIntegerList("RecipeSlots");
        if (recipeSlots.isEmpty()) {
            recipeSlots = DEFAULT_RECIPE_SLOTS;
        }
        int perPage = recipeSlots.size();
        int maxPage = Math.max(0, (int) Math.ceil((double) station.getRecipes().size() / perPage) - 1);
        page = Math.max(0, Math.min(page, maxPage));
        ((PlayerListHolder) inv.getHolder()).setPage(page);

        int startIndex = page * perPage;
        List<StationRecipe> recipes = station.getRecipes();
        for (int i = 0; i < recipeSlots.size(); i++) {
            int recipeIndex = startIndex + i;
            int slot = recipeSlots.get(i);
            if (recipeIndex >= recipes.size() || slot >= inv.getSize()) {
                continue;
            }
            StationRecipe recipe = recipes.get(recipeIndex);
            ItemStack display = buildDisplayItem(player, recipe);
            inv.setItem(slot, display);
        }

        placeButton(inv, section.getConfigurationSection("Items.Close"));
        if (page < maxPage) {
            placeButton(inv, section.getConfigurationSection("Items.Next"));
        }
        if (page > 0) {
            placeButton(inv, section.getConfigurationSection("Items.Previous"));
        }

        player.openInventory(inv);
    }

    private ItemStack buildDisplayItem(Player player, StationRecipe recipe) {
        ItemStack base = safeClone(recipe.getResultItem(), true);
        ItemMeta meta = base.getItemMeta();
        List<Component> lore = StationLoreBuilder.buildPlayerRecipeLore(plugin, recipe, player);
        if (meta != null) {
            meta.lore(lore);
            base.setItemMeta(meta);
        }
        if (base.getType() == Material.AIR) return base;
        NBT.modify(base, (Consumer<ReadWriteItemNBT>) nbt -> {
            nbt.setString("NeoStationId", station.getId());
            nbt.setString("NeoStationRecipe", recipe.getKey());
            nbt.setString("NeoStationView", "PLAYER_LIST");
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

    private void placeButton(Inventory inv, ConfigurationSection buttonSection) {
        if (buttonSection == null) {
            return;
        }
        ItemStack button = decorateButton(ConfigItemBuilder.fromSection(plugin, buttonSection), buttonSection.getString("Action"));
        placeIntoSlots(inv, buttonSection, button);
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
            if (slot == null) {
                continue;
            }
            if (slot < 0 || slot >= inv.getSize()) {
                continue;
            }
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

    public static class PlayerListHolder implements InventoryHolder {

        private final String stationId;
        private int page;

        public PlayerListHolder(String stationId, int page) {
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
