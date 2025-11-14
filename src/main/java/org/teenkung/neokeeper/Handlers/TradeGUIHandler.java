package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBT;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.teenkung.neokeeper.ConfigLoader;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryStorage;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.ItemComparison;

import java.util.HashMap;

import static java.lang.Math.max;

@SuppressWarnings({"DuplicatedCode", "DataFlowIssue"})
public class TradeGUIHandler implements Listener {

    private final NeoKeeper plugin;
    private final ConfigLoader configLoader;
    private final HashMap<Player, Long> lastTrade = new HashMap<>();

    public TradeGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.configLoader = plugin.getConfigLoader();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (TradeInventoryManager.isPluginInventory(event.getInventory())) {
            ItemStack q1 = event.getInventory().getItem(plugin.getConfigLoader().getQuest1Slot());
            ItemStack q2 = event.getInventory().getItem(plugin.getConfigLoader().getQuest2Slot());

            if (q1 != null) {
                event.getPlayer().getInventory().addItem(q1);
            }
            if (q2 != null) {
                event.getPlayer().getInventory().addItem(q2);
            }

            TradeInventoryManager.removeInventory(event.getInventory());
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = player.getOpenInventory().getTopInventory();
        Inventory clickedInventory = event.getClickedInventory();
        boolean clickedPluginInventory = clickedInventory != null && TradeInventoryManager.isPluginInventory(clickedInventory);
        ItemStack currentItem = event.getCurrentItem();
        Integer slot = event.getSlot();
        if (!TradeInventoryManager.isPluginInventory(inventory)) {
            return;
        }
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
        if ((!slot.equals(configLoader.getQuest1Slot()) && !slot.equals(configLoader.getQuest2Slot()) && !slot.equals(configLoader.getRewardSlot())) && clickedPluginInventory) {
            event.setCancelled(true);
        }

        TradeInventoryStorage storage = TradeInventoryManager.getInventoryStorage(inventory);
        String id = storage.id();
        InventoryManager inventoryManager = plugin.getShopManager().getTradeManager(id);

        if (currentItem != null) {
            if (NBT.get(currentItem, nbt -> { return nbt.hasTag("NeoShopID"); })) {
                event.setCancelled(true);
            }
            if (event.getClickedInventory() == player.getInventory()) {
                if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                    if (ItemComparison.matches(new ItemManager(currentItem), inventoryManager.getTradeManagers().get(storage.selecting()).getRewardManager())) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (handleNextPageEvent(event, storage, inventoryManager)) {
            player.playSound(player.getLocation(), Sound.BLOCK_AMETHYST_BLOCK_HIT, 1, 1);
            return;
        }
        else if (handleSelectorEvent(event, storage, inventoryManager)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        else if (handleQuestEvent(event, storage, inventoryManager)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        else if (handleTradeEvent(event, storage, inventoryManager)) {
            deductItem(event, inventoryManager.getTradeManagers().get(storage.selecting()));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 0.25F, 2);
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
    }

    private boolean handleNextPageEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        if (!TradeInventoryManager.isPluginInventory(event.getClickedInventory())) return false;
        Integer offset = storage.offset();
        if (configLoader.getNextPageSlots().contains(event.getSlot())) {
            offset = Math.min(inventoryManager.getTradeManagers().size() - 1, offset + 1);
            storage.offset(offset);
            inventoryManager.getTradeGUI().fillSelector((Player) event.getWhoClicked(), offset);
            return true;
        }
        if (configLoader.getPreviousPageSlots().contains(event.getSlot())) {
            offset = max(0, offset - 1);
            storage.offset(offset);
            inventoryManager.getTradeGUI().fillSelector((Player) event.getWhoClicked(), offset);
            return true;
        }
        return false;
    }
    private boolean handleSelectorEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !TradeInventoryManager.isPluginInventory(clickedInventory)) {
            return false;
        }
        if (event.getCurrentItem() == null) {
            return false;
        }
        if (!configLoader.getAllSelectors().contains(event.getSlot())) {
            return false;
        }
        if (!NBT.get(event.getCurrentItem(), nbt -> { return nbt.hasTag("NeoIndex"); })) {
            return false;
        }

        Integer index = NBT.get(event.getCurrentItem(), nbt -> { return nbt.getInteger("NeoIndex"); });
        storage.selecting(index);
        prepareItem(event, inventoryManager, storage, true);
        return true;
    }
    private boolean handleQuestEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !TradeInventoryManager.isPluginInventory(clickedInventory)) {
            return false;
        }
        if (event.getSlot() != configLoader.getQuest1Slot() && event.getSlot() != configLoader.getQuest2Slot()) {
            return false;
        }
        ItemManager q1Item = new ItemManager(clickedInventory.getItem(configLoader.getQuest1Slot()));
        ItemManager q2Item = new ItemManager(clickedInventory.getItem(configLoader.getQuest2Slot()));
        for (int i = 0 ; i < inventoryManager.getTradeManagers().size() ; i++) {
            TradeManager tradeManager = inventoryManager.getTradeManagers().get(i);
            if (ItemComparison.matches(q1Item, tradeManager.getQuest1Manager()) && ItemComparison.matches(q2Item, tradeManager.getQuest2Manager())) {
                storage.selecting(i);
                return true;
            }
        }
        return false;
    }
    private boolean handleTradeEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null || !TradeInventoryManager.isPluginInventory(clickedInventory)) {
            return false;
        }
        if (event.getSlot() != configLoader.getRewardSlot()) {
            return false;
        }

        if (event.getAction() != InventoryAction.PICKUP_ALL && event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY && event.getAction() != InventoryAction.PLACE_ONE && event.getAction() != InventoryAction.PLACE_ALL) {
            event.setCancelled(true);
            return false;
        }

        if (event.getCurrentItem() == null) {
            event.setCancelled(true);
            return false;
        }

        if (NBT.get(event.getCurrentItem(), nbt -> { return nbt.hasTag("NeoShopID"); })) {
            event.setCancelled(true);
            return false;
        }

        Player player = (Player) event.getWhoClicked();
        if (lastTrade.containsKey(player) && System.currentTimeMillis() - lastTrade.get(player) < 60) {
            player.sendMessage(plugin.colorize("<red>Please wait before trading again!"));
            event.setCancelled(true);
            return false;
        }

        ItemStack q1item = clickedInventory.getItem(configLoader.getQuest1Slot());
        ItemStack q2item = clickedInventory.getItem(configLoader.getQuest2Slot());

        boolean itemsValid = checkItems(q1item, q2item, inventoryManager.getTradeManagers().get(storage.selecting()));

        if (!itemsValid) {
            event.getWhoClicked().sendMessage(MiniMessage.miniMessage().deserialize("<red>How?"));
            event.setCancelled(true);
            return false;
        }

        ItemManager rewardManager = inventoryManager.getTradeManagers().get(storage.selecting()).getRewardManager();

        // prevent player putting item back to reward slot
        if (event.getAction() == InventoryAction.PLACE_ONE || event.getAction() == InventoryAction.PLACE_ALL) {
            event.setCancelled(true);
            if (ItemComparison.matches(new ItemManager(event.getCursor()), rewardManager)) {
                if (event.getCursor().getMaxStackSize() >= event.getCursor().getAmount() + rewardManager.getAmount()) {
                    event.getCursor().setAmount(event.getCursor().getAmount() + rewardManager.getAmount());
                    return true;
                }
                return false;
            }
            return false;
        }

        // shift click functionality
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            while (checkItems(clickedInventory.getItem(configLoader.getQuest1Slot()), clickedInventory.getItem(configLoader.getQuest2Slot()), inventoryManager.getTradeManagers().get(storage.selecting()))) {
                if (event.getWhoClicked().getInventory().firstEmpty() == -1) break;
                event.getWhoClicked().getInventory().addItem(clickedInventory.getItem(configLoader.getRewardSlot()));
                deductItem(event, inventoryManager.getTradeManagers().get(storage.selecting()));
            }
            clickedInventory.setItem(configLoader.getRewardSlot(), plugin.getNoItemItem());
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(clickedInventory, storage, inventoryManager), 1);
            return false;
        }

        return true;
    }

    private void showReward(Inventory inventory, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        if (inventoryManager.getTradeManagers().isEmpty()) {
            return;
        }
        TradeManager tradeManager = inventoryManager.getTradeManagers().get(storage.selecting());
        ItemStack reward = plugin.getNoItemItem();
        ItemStack q1item = inventory.getItem(configLoader.getQuest1Slot());
        ItemStack q2item = inventory.getItem(configLoader.getQuest2Slot());

        boolean itemsValid = checkItems(q1item, q2item, tradeManager);

        if (itemsValid) {
            if (inventory.getItem(configLoader.getRewardSlot()) != null && !NBT.get(inventory.getItem(configLoader.getRewardSlot()), nbt -> { return nbt.hasTag("NeoShopID"); })) {
                return;
            }
            reward = tradeManager.getRewardItem();
        }
        inventory.setItem(configLoader.getRewardSlot(), reward);
    }

    private boolean checkItems(ItemStack q1item, ItemStack q2item, TradeManager tradeManager) {
        int q1amt = q1item == null ? 0 : q1item.getAmount();
        int q2amt = q2item == null ? 0 : q2item.getAmount();
        ItemManager q1Manager = tradeManager.getQuest1Manager();
        ItemManager q2Manager = tradeManager.getQuest2Manager();
        boolean q1 = false;
        boolean q2 = false;

        if (ItemComparison.matches(new ItemManager(q1item), q1Manager) && q1amt >= q1Manager.getAmount()) {
            q1 = true;
        }
        if (ItemComparison.matches(new ItemManager(q2item), q2Manager) && q2amt >= q2Manager.getAmount()) {
            q2 = true;
        }

        return q1 && q2;
    }

    private void prepareItem(InventoryClickEvent event, InventoryManager inventoryManager, TradeInventoryStorage storage, boolean playSound) {
        Inventory clickedInv = event.getClickedInventory();
        Player player = (Player) event.getWhoClicked();
        PlayerInventory playerInv = player.getInventory();
        TradeManager tradeManager = inventoryManager.getTradeManagers().get(storage.selecting());

        // Return original quest items to player (if present)
        ItemStack quest1Original = clickedInv.getItem(configLoader.getQuest1Slot());
        if (quest1Original != null && !quest1Original.getType().isAir()) {
            playerInv.addItem(quest1Original);
        }
        ItemStack quest2Original = clickedInv.getItem(configLoader.getQuest2Slot());
        if (quest2Original != null && !quest2Original.getType().isAir()) {
            playerInv.addItem(quest2Original);
        }

        // Clear quest slots and reset reward
        clickedInv.setItem(configLoader.getQuest1Slot(), null);
        clickedInv.setItem(configLoader.getQuest2Slot(), null);
        clickedInv.setItem(configLoader.getRewardSlot(), plugin.getNoItemItem());

        // Prepare quest item managers and checks
        ItemManager q1Manager = tradeManager.getQuest1Manager();
        ItemManager q2Manager = tradeManager.getQuest2Manager();
        boolean q1None = q1Manager.getStringItem().equalsIgnoreCase("NONE");
        boolean q2None = q2Manager.getStringItem().equalsIgnoreCase("NONE");

        boolean completed_1 = false;
        boolean completed_2 = false;

        // Attempt to find and move quest items from player's inventory
        ItemStack[] contents = playerInv.getContents();
        for (int i = 0; i < contents.length; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType().isAir()) continue;

            // Check quest 1
            if (!completed_1 && (q1None || ItemComparison.matches(new ItemManager(item), q1Manager))) {
                if (!q1None) {
                    playerInv.setItem(i, null);
                    clickedInv.setItem(configLoader.getQuest1Slot(), item);
                }
                completed_1 = true;
                continue;
            }

            // Check quest 2
            if (!completed_2 && (q2None || ItemComparison.matches(new ItemManager(item), q2Manager))) {
                if (!q2None) {
                    clickedInv.setItem(configLoader.getQuest2Slot(), item);
                    playerInv.setItem(i, null);
                }
                completed_2 = true;
                continue;
            }

            // Break early if both quests are satisfied
            if (completed_1 && completed_2) {
                if (playSound) {
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.33F, 1);
                    player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 2);
                }
                return;
            }
        }

        // If we reach here, not both quests were completed
        if (playSound) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.33F, 1);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 1, 1);
        }
    }


    private void deductItem(InventoryClickEvent event, TradeManager manager) {
        ItemStack q1 = event.getClickedInventory().getItem(configLoader.getQuest1Slot());
        ItemStack q2 = event.getClickedInventory().getItem(configLoader.getQuest2Slot());
        if (q1 != null) {
            event.getClickedInventory().getItem(configLoader.getQuest1Slot()).setAmount(q1.getAmount() - manager.getQuest1Manager().getAmount());
        }
        if (q2 != null) {
            event.getClickedInventory().getItem(configLoader.getQuest2Slot()).setAmount(q2.getAmount() - manager.getQuest2Manager().getAmount());
        }
    }

}
