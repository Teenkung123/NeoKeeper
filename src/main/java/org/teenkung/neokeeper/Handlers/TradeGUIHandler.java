package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBT;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.ConfigLoader;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryStorage;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;

import static java.lang.Math.max;

@SuppressWarnings({"DuplicatedCode", "DataFlowIssue"})
public class TradeGUIHandler implements Listener {

    private final NeoKeeper plugin;
    private final ConfigLoader configLoader;

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
        ItemStack currentItem = event.getCurrentItem();
        Integer slot = event.getSlot();
        if (!TradeInventoryManager.isPluginInventory(inventory)) {
            return;
        }
        if (event.getAction() == InventoryAction.COLLECT_TO_CURSOR) {
            event.setCancelled(true);
        }
        if ((!slot.equals(configLoader.getQuest1Slot()) && !slot.equals(configLoader.getQuest2Slot()) && !slot.equals(configLoader.getRewardSlot())) && TradeInventoryManager.isPluginInventory(clickedInventory)) {
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
                    if (compare(new ItemManager(currentItem), inventoryManager.getTradeManagers().get(storage.selecting()).getRewardManager())) {
                        event.setCancelled(true);
                    }
                }
            }
        }

        if (handleNextPageEvent(event, storage, inventoryManager)) {
            return;
        }
        if (handleSelectorEvent(event, storage, player, inventoryManager)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        if (handleQuestEvent(event, storage, inventoryManager)) {
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        if (handleTradeEvent(event, storage, inventoryManager)) {
            deductItem(event, inventoryManager.getTradeManagers().get(storage.selecting()));
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
            return;
        }
        Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(inventory, storage, inventoryManager), 1);
    }

    private boolean handleNextPageEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        Integer offset = storage.offset();
        if (configLoader.getNextPageSlots().contains(event.getSlot())) {
            offset = Math.min(inventoryManager.getTradeManagers().size() - 4, offset + configLoader.getSelectorSlots().size());
            storage.offset(offset);
            inventoryManager.getTradeGUI().fillSelector((Player) event.getWhoClicked(), offset);
            return true;
        }
        if (configLoader.getPreviousPageSlots().contains(event.getSlot())) {
            offset = max(0, offset - configLoader.getSelectorSlots().size());
            storage.offset(offset);
            inventoryManager.getTradeGUI().fillSelector((Player) event.getWhoClicked(), offset);
            return true;
        }
        return false;
    }
    private boolean handleSelectorEvent(InventoryClickEvent event, TradeInventoryStorage storage, Player player, InventoryManager inventoryManager) {
        if (event.getCurrentItem() == null) {
            return false;
        }
        if (!configLoader.getAllSelectors().contains(event.getSlot())) {
            return false;
        }
        if (!NBT.get(event.getCurrentItem(), nbt -> { return nbt.hasTag("NeoIndex"); })) {
            return false;
        }
        player.playSound(player.getLocation(), org.bukkit.Sound.UI_BUTTON_CLICK, 1, 1);
        Integer index = NBT.get(event.getCurrentItem(), nbt -> { return nbt.getInteger("NeoIndex"); });
        storage.selecting(index);
        prepareItem(event, inventoryManager, storage);
        return true;
    }
    private boolean handleQuestEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        if (event.getSlot() != configLoader.getQuest1Slot() && event.getSlot() != configLoader.getQuest2Slot()) {
            return false;
        }
        ItemManager q1Item = new ItemManager(event.getClickedInventory().getItem(configLoader.getQuest1Slot()));
        ItemManager q2Item = new ItemManager(event.getClickedInventory().getItem(configLoader.getQuest2Slot()));
        for (int i = 0 ; i < inventoryManager.getTradeManagers().size() ; i++) {
            TradeManager tradeManager = inventoryManager.getTradeManagers().get(i);
            if (compare(q1Item, tradeManager.getQuest1Manager()) && compare(q2Item, tradeManager.getQuest2Manager())) {
                storage.selecting(i);
                return true;
            }
        }
        return false;
    }
    private boolean handleTradeEvent(InventoryClickEvent event, TradeInventoryStorage storage, InventoryManager inventoryManager) {
        if (event.getSlot() != configLoader.getRewardSlot()) {
            return false;
        }

        if (event.getAction() != InventoryAction.PICKUP_ALL && event.getAction() != InventoryAction.MOVE_TO_OTHER_INVENTORY) {
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

        // prevent player putting item back to reward slot
        if (!event.getCursor().getType().isAir()) {
            event.setCancelled(true);
            if (compare (new ItemManager(event.getCursor()), inventoryManager.getTradeManagers().get(storage.selecting()).getRewardManager())) {
                if (event.getCursor().getMaxStackSize() > event.getCursor().getAmount() + 1) {
                    event.getCursor().setAmount(event.getCursor().getAmount() + 1);
                    return true;
                }
                return false;
            }
            return false;
        }

        // shift click functionality
        if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
            while (checkItems(event.getClickedInventory().getItem(configLoader.getQuest1Slot()), event.getClickedInventory().getItem(configLoader.getQuest2Slot()), inventoryManager.getTradeManagers().get(storage.selecting()))) {
                if (event.getWhoClicked().getInventory().firstEmpty() == -1) break;
                event.getWhoClicked().getInventory().addItem(event.getClickedInventory().getItem(configLoader.getRewardSlot()));
                deductItem(event, inventoryManager.getTradeManagers().get(storage.selecting()));
            }
            event.getClickedInventory().setItem(configLoader.getRewardSlot(), plugin.getNoItemItem());
            Bukkit.getScheduler().runTaskLater(plugin, () -> showReward(event.getClickedInventory(), storage, inventoryManager), 1);
            return false;
        }

        return true;
    }

    private void showReward(Inventory inventory, TradeInventoryStorage storage, InventoryManager inventoryManager) {
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

        if (compare(new ItemManager(q1item), q1Manager) && q1amt >= q1Manager.getAmount()) {
            q1 = true;
        }
        if (compare(new ItemManager(q2item), q2Manager) && q2amt >= q2Manager.getAmount()) {
            q2 = true;
        }

        return q1 && q2;
    }

    private void prepareItem(InventoryClickEvent event, InventoryManager inventoryManager, TradeInventoryStorage storage) {
        if (event.getClickedInventory().getItem(configLoader.getQuest1Slot()) != null) {
            event.getWhoClicked().getInventory().addItem(event.getClickedInventory().getItem(configLoader.getQuest1Slot()));
        }
        if (event.getClickedInventory().getItem(configLoader.getQuest2Slot()) != null) {
            event.getWhoClicked().getInventory().addItem(event.getClickedInventory().getItem(configLoader.getQuest2Slot()));
        }
        event.getClickedInventory().setItem(configLoader.getQuest1Slot(), new ItemStack(Material.AIR));
        event.getClickedInventory().setItem(configLoader.getQuest2Slot(), new ItemStack(Material.AIR));
        event.getClickedInventory().setItem(configLoader.getRewardSlot(), plugin.getNoItemItem());
        TradeManager tradeManager = inventoryManager.getTradeManagers().get(storage.selecting());
        boolean completed_1 = false;
        boolean completed_2 = false;
        for (int i = 0 ; i < event.getWhoClicked().getInventory().getSize() ; i++) {
            ItemStack item = event.getWhoClicked().getInventory().getItem(i);
            if (item == null) continue;
            if (item.getType().isAir()) continue;
            if (compare(new ItemManager(item), tradeManager.getQuest1Manager()) && !completed_1) {
                event.getWhoClicked().getInventory().setItem(i, new ItemStack(Material.AIR));
                event.getClickedInventory().setItem(configLoader.getQuest1Slot(), item);
                completed_1 = true;
            }
            if (compare(new ItemManager(item), tradeManager.getQuest2Manager()) && !completed_2) {
                event.getClickedInventory().setItem(configLoader.getQuest2Slot(), item);
                event.getWhoClicked().getInventory().setItem(i, new ItemStack(Material.AIR));
                completed_2 = true;
            }
            if (completed_1 && completed_2) {
                break;
            }
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

    private boolean compare(ItemManager item1, ItemManager item2) {
        return item1.getStringItem().equals(item2.getStringItem());
    }
}