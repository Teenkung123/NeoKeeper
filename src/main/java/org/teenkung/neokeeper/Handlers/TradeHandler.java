package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.ShopManager;
import org.teenkung.neokeeper.Managers.TradeManager;
import org.teenkung.neokeeper.NeoKeeper;


public class TradeHandler implements Listener {

    private final NeoKeeper plugin;
    private final FileConfiguration mainConfig;

    public TradeHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfig();
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            if (event.getCurrentItem().getType() != Material.AIR) {
                Player player = (Player) event.getWhoClicked();
                NBTItem nbt = new NBTItem(event.getCurrentItem());
                if (nbt.hasTag("NeoShopID")) {
                    String id = nbt.getString("NeoShopID");
                    if (nbt.hasTag("NeoIndex")) {
                        Integer index = nbt.getInteger("NeoIndex");
                        Inventory inv = event.getClickedInventory();
                        if (inv == null) {
                            return;
                        }
                        ItemStack rewardSlotItem = inv.getItem(mainConfig.getInt("GUI.Slot.Reward"));
                        if (rewardSlotItem != null) {
                            player.playSound(player, Sound.BLOCK_DISPENSER_DISPENSE, 1, 1);

                            if (rewardSlotItem != plugin.getNoItemItem(id)) {
                                player.getInventory().addItem(rewardSlotItem);
                            }

                            ShopManager manager = plugin.getShopLoader().getShopManager(id);

                            TradeManager tradeManager = manager.getTradeManagers().get(index);
                            ItemManager req1Manager = tradeManager.getQuest1Manager();
                            ItemManager req2Manager = tradeManager.getQuest2Manager();
                            String req1Type = req1Manager.getType();
                            String req2Type = req2Manager.getType();

                            ItemStack q1Item = inv.getItem(mainConfig.getInt("GUI.Slot.Quest1"));
                            ItemStack q2Item = inv.getItem(mainConfig.getInt("GUI.Slot.Quest2"));
                            trade(inv, req1Type, req2Type, q1Item, q2Item, req1Manager, req2Manager, tradeManager);
                        }
                    }
                }
            }
        }
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

    private boolean trade(Inventory inv, String req1Type, String req2Type, ItemStack q1Item, ItemStack q2Item, ItemManager req1Manager, ItemManager req2Manager, TradeManager tradeManager) {
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

        if (q1Pass && q2Pass) {
            if (q1Item != null && !req1Type.equalsIgnoreCase("NONE")) {
                Bukkit.broadcastMessage(q1Item.getAmount() + " - " + req1Manager.getAmount() + " = " + (q1Item.getAmount()-req1Manager.getAmount()));
                q1Item.setAmount(q1Item.getAmount()-req1Manager.getAmount());
            }
            if (q2Item != null && !req2Type.equalsIgnoreCase("NONE")) {
                Bukkit.broadcastMessage(q2Item.getAmount() + " - " + req2Manager.getAmount() + " = " + (q2Item.getAmount()-req2Manager.getAmount()));
                q2Item.setAmount(q2Item.getAmount()-req1Manager.getAmount());
            }

            ItemStack reward = tradeManager.getRewardItem();

            inv.setItem(plugin.getConfig().getInt("GUI.Slot.Reward"), reward);
            return true;

        }
        return false;
    }
}
