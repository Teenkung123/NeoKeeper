package org.teenkung.neokeeper.GUIs;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EditGUI {
    // The InventoryManager (which holds the list of TradeManagers) for this shop.
    public final InventoryManager inventoryManager;
    private final NeoKeeper plugin;

    // Control button items:
    private final ItemStack renameItem;
    private final ItemStack saveItem;
    private final ItemStack deleteItem;
    private final ItemStack prevPageItem;
    private final ItemStack nextPageItem;

    // Layout constants:
    public static final int INVENTORY_SIZE = 36; // 4 rows * 9 slots
    public static final int TRADES_PER_PAGE = 9;     // one trade per column (columns 0-8)

    // A temporary cache to store (edited) inventories for each page.
    private final Map<Integer, Inventory> pageCache = new HashMap<>();

    public EditGUI(InventoryManager inventoryManager) {
        this.inventoryManager = inventoryManager;
        this.plugin = inventoryManager.getPlugin();

        // Create control items (using your ItemManager and colorize method)
        renameItem = createControlItem(Material.OAK_SIGN, "<yellow>Rename Shop", List.of(
                "<white>Click to change the shop title."
        ));
        saveItem = createControlItem(Material.LIME_CONCRETE, "<green>Save Changes", List.of(
                "<white>Click to save your changes."
        ));
        deleteItem = createControlItem(Material.RED_CONCRETE, "<red>Delete Shop", List.of(
                "<white>Click to delete the shop.",
                "<red>WARNING: Irreversible!"
        ));
        prevPageItem = createControlItem(Material.ARROW, "<yellow>Previous Page", List.of(
                "<white>Click to view the previous page."
        ));
        nextPageItem = createControlItem(Material.ARROW, "<yellow>Next Page", List.of(
                "<white>Click to view the next page."
        ));
    }

    private ItemStack createControlItem(Material material, String displayName, List<String> loreLines) {
        ItemManager itemManager = new ItemManager(new ItemStack(material));
        itemManager.setDisplayName(plugin.colorize(displayName));
        List<Component> loreComponents = loreLines.stream().map(plugin::colorize).toList();
        itemManager.setLore(loreComponents);
        return itemManager.getItem();
    }

    /**
     * Computes the total number of pages based on the number of trades from the configuration
     * and any pages the user has opened in this editing session.
     */
    public int getTotalPages() {
        int computedPages = (inventoryManager.getTradeManagers().size() + TRADES_PER_PAGE - 1) / TRADES_PER_PAGE;
        // The cached pages might include an extra (empty) page.
        int maxCached = pageCache.keySet().stream().max(Integer::compareTo).orElse(0) + 1;
        return Math.max(Math.max(1, computedPages), maxCached);
    }

    /**
     * Opens the edit GUI for the given player on page 0.
     */
    public void openEditGUI(Player player) {
        openEditGUI(player, 0);
    }

    /**
     * Opens the edit GUI for the given player on the specified page.
     */
    public void openEditGUI(Player player, int page) {
        Inventory inv = getPage(page);
        player.openInventory(inv);
    }

    /**
     * Returns the cached Inventory for a page if available; otherwise creates a new one.
     */
    public Inventory getPage(int page) {
        if (pageCache.containsKey(page)) {
            return pageCache.get(page);
        }
        int totalPages = getTotalPages();
        String id = inventoryManager.getId();
        InventoryHolder holder = new EditInventoryHolder(id, page);
        Inventory inv = Bukkit.createInventory(holder, INVENTORY_SIZE,
                Component.text("Editing: " + id + " (Page " + (page + 1) + "/" + totalPages + ")"));

        // --- Fill the trade area (rows 0-2) ---
        // Each column (0 to 8) represents one trade.
        List<TradeManager> tradeManagers = inventoryManager.getTradeManagers();
        int startIndex = page * TRADES_PER_PAGE;
        for (int col = 0; col < TRADES_PER_PAGE; col++) {
            int tradeIndex = startIndex + col;
            if (tradeIndex < tradeManagers.size()) {
                TradeManager trade = tradeManagers.get(tradeIndex);
                // Row 1 (slots 0-8): Reward
                inv.setItem(col, trade.getRewardItem());
                // Row 2 (slots 9-17): Quest1
                inv.setItem(9 + col, trade.getQuest1Item());
                // Row 3 (slots 18-26): Quest2
                inv.setItem(18 + col, trade.getQuest2Item());
            }
        }

        // --- Set up the control row (row 4, slots 27-35) ---
        // Slot 27: Previous page (if not on the first page)
        if (page > 0) {
            inv.setItem(27, prevPageItem);
        }
        // Slot 30: Rename button
        inv.setItem(30, renameItem);
        // Slot 31: Save button
        inv.setItem(31, saveItem);
        // Slot 32: Delete button
        inv.setItem(32, deleteItem);
        // Slot 35: Next page (ALWAYS displayed so the user can create a new page immediately)
        inv.setItem(35, nextPageItem);

        // Cache this page for later retrieval.
        pageCache.put(page, inv);
        return inv;
    }

    /**
     * Iterates through all pages (from 0 up to the maximum of computed and cached pages)
     * and saves the trade data. Only trades where the reward slot is not empty are saved.
     */
    public void saveAllPages() {
        YamlConfiguration config = inventoryManager.getConfig();
        String id = inventoryManager.getId();
        config.set("Items", null);

        // Determine the last page index to check by comparing the computed pages and cached pages.
        int computedPages = (inventoryManager.getTradeManagers().size() + TRADES_PER_PAGE - 1) / TRADES_PER_PAGE;
        int maxCached = pageCache.keySet().stream().max(Integer::compareTo).orElse(0);
        int lastPage = Math.max(computedPages - 1, maxCached);

        int tradeIndex = 0;
        for (int page = 0; page <= lastPage; page++) {
            Inventory inv = pageCache.containsKey(page) ? pageCache.get(page) : getPage(page);
            // For each trade (each column in the trade area)
            for (int col = 0; col < TRADES_PER_PAGE; col++) {
                // row 1 (index 0)
                int slotQuest1 = 9 + col;    // row 2
                int slotQuest2 = 18 + col;   // row 3

                ItemStack rewardItem = inv.getItem(col);
                // Only save if a reward item is present.
                if (rewardItem == null || rewardItem.getType() == Material.AIR) {
                    continue;
                }
                ItemStack quest1Item = inv.getItem(slotQuest1);
                ItemStack quest2Item = inv.getItem(slotQuest2);

                ConfigurationSection section = config.createSection("Items." + tradeIndex);
                ItemManager rewardManager = new ItemManager(rewardItem);
                ItemManager quest1Manager = new ItemManager(quest1Item);
                ItemManager quest2Manager = new ItemManager(quest2Item);

                setSection(section, "Reward", rewardManager);
                setSection(section, "Quests.1", quest1Manager);
                setSection(section, "Quests.2", quest2Manager);

                tradeIndex++;
            }
        }
        try {
            config.save(new File(plugin.getDataFolder(), "Shops/" + id + ".yml"));
            plugin.reload();
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save shop configuration for " + id);
        }
    }

    private void setSection(ConfigurationSection section, String path, ItemManager item) {
        section.set(path + ".Type", item.getType());
        section.set(path + ".Item", item.getStringItem());
        section.set(path + ".Amount", item.getAmount());
    }

    /**
     * A custom InventoryHolder used to tag an inventory with its shop id and page number.
     */
    public record EditInventoryHolder(String shopId, int page) implements InventoryHolder {
        @SuppressWarnings("NullableProblems")
        @Override
        public Inventory getInventory() {
            return null;
        }
    }
}
