package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.ConfigLoader;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryManager;
import org.teenkung.neokeeper.Managers.Trades.TradeGUIUtils;
import org.teenkung.neokeeper.Managers.Trades.TradeInventoryStorage;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;


public class TradeGUIHandler implements Listener {

    private final NeoKeeper plugin;
    private final ConfigLoader config;

    public TradeGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigLoader();
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (TradeInventoryManager.isPluginInventory(event.getClickedInventory()) && event.getClickedInventory() != null) {
            if (event.getCurrentItem() == null) {
                return;
            }
            if (event.getCurrentItem().getType() == Material.AIR) {
                return;
            }
            event.setCancelled(true);
            if (event.getSlot() == config.getQuest1Slot() || event.getSlot() == config.getQuest2Slot() || event.getSlot() == config.getRewardSlot()) {
                event.setCancelled(false);
            }

            Inventory inv = event.getClickedInventory();
            TradeInventoryStorage invStorage = TradeInventoryManager.getInventoryStorage(inv);
            Player player = (Player) event.getWhoClicked();
            Integer offset = invStorage.offset();
            String id = invStorage.id();
            TradeGUIUtils tradeGUIUtils = plugin.getShopLoader().getTradeManager(id);
            if (config.getNextPageSlots().contains(event.getSlot())) {
                offset = Math.min(tradeGUIUtils.getTradeManagers().size() - 4, offset + config.getSelectorSlots().size());
                invStorage.offset(offset);
                tradeGUIUtils.fillSelector(player, offset);
            } else if (config.getPreviousPageSlots().contains(event.getSlot())) {
                offset = Math.max(0, offset - config.getSelectorSlots().size());
                invStorage.offset(offset);
                tradeGUIUtils.fillSelector(player, offset);
            } else if (config.getAllSelectors().contains(event.getSlot())) {
                player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);
                NBTItem nbt = new NBTItem(event.getCurrentItem());
                if (!nbt.hasTag("NeoIndex")) { return; }
                Integer index = nbt.getInteger("NeoIndex");
                invStorage.selecting(index);
                performTrade(player,inv, invStorage, id, tradeGUIUtils);
            } else if (config.getRewardSlot().equals(event.getSlot())) {
                ItemStack clickedItems = event.getCurrentItem();
                NBTItem nbt = new NBTItem(clickedItems);
                if (nbt.hasTag("NeoShopID")) {
                    event.setCancelled(true);
                } else {
                    Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        if (inv.getItem(config.getRewardSlot()) == null) { inv.setItem(config.getRewardSlot(), plugin.getNoItemItem()); }
                    }, 1);
                }
            }
        }
    }

    private void performTrade(Player player, Inventory inv, TradeInventoryStorage invStorage, String id, TradeGUIUtils tradeGUIUtils) {
        TradeManager tradeManager = tradeGUIUtils.getTradeManagers().get(invStorage.selecting());
        ItemManager req1Manager = tradeManager.getQuest1Manager();
        ItemManager req2Manager = tradeManager.getQuest2Manager();
        String req1Type = req1Manager.getType();
        String req2Type = req2Manager.getType();

        ItemStack q1Item = inv.getItem(config.getQuest1Slot());
        ItemStack q2Item = inv.getItem(config.getQuest2Slot());
        trade(player, id, inv, req1Type, req2Type, q1Item, q2Item, req1Manager, req2Manager, tradeManager);
    }

    private boolean compare(ItemManager reqManager, ItemStack qItem) {
        if (qItem != null) {
            if (qItem.getType() != Material.AIR) {
                ItemManager qManager = new ItemManager(qItem);
                if (qManager.getStringItem().equals(reqManager.getStringItem())) {
                    return qManager.getAmount() >= reqManager.getAmount();
                }
            }
        }
        return false;
    }

    private void trade(Player player, String id, Inventory inv, String req1Type, String req2Type, ItemStack q1Item, ItemStack q2Item, ItemManager req1Manager, ItemManager req2Manager, TradeManager tradeManager) {
        boolean q1Pass;
        boolean q2Pass;
        if (!req1Type.equalsIgnoreCase("NONE")) {
            q1Pass = compare(req1Manager, q1Item);
        } else {
            q1Pass = true;
        }

        if (!req2Type.equalsIgnoreCase("NONE")) {
            q2Pass = compare(req2Manager, q2Item);
        } else {
            q2Pass = true;
        }
        ItemStack rewardSlot = inv.getItem(config.getRewardSlot());
        if (q1Pass && q2Pass) {
            boolean takeItem = true;
            ItemStack reward = tradeManager.getRewardItem();
            ItemManager rewardManager = tradeManager.getRewardManager();
            if (rewardSlot != null) {
                ItemManager rewardSlotManager = new ItemManager(rewardSlot);
                NBTItem rewardSlotNBT = new NBTItem(rewardSlot);
                if (rewardSlotNBT.hasTag("NeoShopID")) {
                    inv.setItem(config.getRewardSlot(), reward);
                } else if ((rewardManager.getStringItem().equalsIgnoreCase(rewardSlotManager.getStringItem())) && (rewardManager.getType().equalsIgnoreCase(rewardSlotManager.getType()))) {
                    if (rewardSlot.getAmount() + reward.getAmount() <= 64) {
                        reward.setAmount(rewardSlot.getAmount() + reward.getAmount());
                        inv.setItem(config.getRewardSlot(), reward);
                    } else {
                        takeItem = false;
                    }
                } else {
                    player.getInventory().addItem(rewardSlot);
                    inv.setItem(config.getRewardSlot(), reward);
                }
            } else {
                inv.setItem(config.getRewardSlot(), reward);
            }
            if (takeItem) {
                if (q1Item != null && !req1Type.equalsIgnoreCase("NONE")) {
                    q1Item.setAmount(q1Item.getAmount()-req1Manager.getAmount());
                }
                if (q2Item != null && !req2Type.equalsIgnoreCase("NONE")) {
                    q2Item.setAmount(q2Item.getAmount()-req1Manager.getAmount());
                }
            }
        } else {
            if (rewardSlot != null) {
                NBTItem rewardSlotNBT = new NBTItem(rewardSlot);
                if (rewardSlotNBT.hasTag("NeoShopID")) {
                    inv.setItem(config.getRewardSlot(), plugin.getNoItemItem());
                } else {
                    player.getInventory().addItem(rewardSlot);
                    inv.setItem(config.getRewardSlot(), plugin.getNoItemItem());
                }
            } else {
                inv.setItem(config.getRewardSlot(), plugin.getNoItemItem());
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (TradeInventoryManager.isPluginInventory(event.getInventory())) {
            ItemStack q1 = event.getInventory().getItem(plugin.getConfigLoader().getQuest1Slot());
            ItemStack q2 = event.getInventory().getItem(plugin.getConfigLoader().getQuest2Slot());
            ItemStack r = event.getInventory().getItem(plugin.getConfigLoader().getRewardSlot());

            if (q1 != null) { event.getPlayer().getInventory().addItem(q1); }
            if (q2 != null) { event.getPlayer().getInventory().addItem(q2); }
            if (r != null) {
                NBTItem nbt = new NBTItem(r);
                if (!nbt.hasTag("NeoShopID")) {
                    event.getPlayer().getInventory().addItem(r);
                }
            }


            TradeInventoryManager.removeInventory(event.getInventory());
        }
    }
}
