package org.teenkung.neokeeper.Handlers;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.CitizensUtils;

import java.util.Optional;

public class CitizensNPCListener implements Listener {

    private final NeoKeeper plugin;

    public CitizensNPCListener(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onNpcRightClick(NPCRightClickEvent event) {
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            return;
        }

        Optional<String> shopIdOptional = citizensUtils.getShopId(event.getNPC().getId());
        if (shopIdOptional.isEmpty()) {
            return;
        }

        InventoryManager manager = plugin.getShopManager().getTradeManager(shopIdOptional.get());
        if (manager == null) {
            return;
        }

        Player player = event.getClicker();
        if (player.hasPermission("neokeeper.admin") && player.isSneaking()) {
            manager.getEditGUI().openEditGUI(player);
        } else {
            manager.getTradeGUI().buildTradeGUI(player);
        }
    }

    @EventHandler
    public void onNpcRemove(NPCRemoveEvent event) {
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            return;
        }

        int npcId = event.getNPC().getId();
        Optional<String> shopIdOptional = citizensUtils.getShopId(npcId);

        citizensUtils.unregisterShopkeeper(npcId);

        if (shopIdOptional.isEmpty()) {
            return;
        }

        shopIdOptional.ifPresent(shopId -> plugin.getShopManager().deleteShop(shopId, true));
    }
}

