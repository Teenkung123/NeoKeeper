package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBTItem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.ShopManager;
import org.teenkung.neokeeper.NeoKeeper;


public class GUIHandler implements Listener {

    private final NeoKeeper plugin;
    private final FileConfiguration mainConfig;

    public GUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
        this.mainConfig = plugin.getConfig();
    }

    @EventHandler
    public void onGUIClick(InventoryClickEvent event) {
        if (event.getCurrentItem() != null) {
            if (event.getCurrentItem().getType() != Material.AIR) {
                NBTItem nbt = new NBTItem(event.getCurrentItem());
                if (nbt.hasTag("NeoShopID")) {
                    Player player = (Player) event.getWhoClicked();
                    String id = nbt.getString("NeoShopID");
                    event.setCancelled(true);

                    Inventory inv = event.getClickedInventory();
                    if (inv == null) { return; }

                    if (nbt.hasTag("NeoNextPage")) {
                        Integer offset = nbt.getInteger("NeoOffset");
                        ShopManager manager = plugin.getShopLoader().getShopManager(id);
                        offset = Math.min(manager.getTradeManagers().size()-4, offset+mainConfig.getStringList("GUI.Slot.Lists").size());
                        manager.fillSelector(player, offset);
                        setOffset(inv, offset);

                    } else if (nbt.hasTag("NeoPreviousPage")) {
                        Integer offset = nbt.getInteger("NeoOffset");
                        ShopManager manager = plugin.getShopLoader().getShopManager(id);
                        offset = Math.max(0, offset - mainConfig.getStringList("GUI.Slot.Lists").size());
                        manager.fillSelector(player, offset);
                        setOffset(inv, offset);
                    }
                }
            }
        }
    }

    private void setOffset(Inventory inv, Integer offset) {

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            for (Integer slot : mainConfig.getIntegerList("GUI.Slot.PreviousPage")) {
                setStackOffset(inv, offset, slot);
            }

            for (Integer slot : mainConfig.getIntegerList("GUI.Slot.NextPage")) {
                setStackOffset(inv, offset, slot);
            }
        }, 2);

    }

    private void setStackOffset(Inventory inv, Integer offset, Integer slot) {
        ItemStack stack = inv.getItem(slot);
        if (stack == null) {
            return;
        }
        NBTItem stackNBT = new NBTItem(stack);
        stackNBT.setInteger("NeoOffset", offset);
        stackNBT.applyNBT(stack);
        inv.setItem(slot, stack);
    }
}
