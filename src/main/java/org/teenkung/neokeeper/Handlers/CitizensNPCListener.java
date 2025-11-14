package org.teenkung.neokeeper.Handlers;

import net.citizensnpcs.api.event.NPCRightClickEvent;
import net.citizensnpcs.api.event.NPCRemoveEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.teenkung.neokeeper.Managers.InventoryManager;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
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

        Player player = event.getClicker();

        Optional<String> shopIdOptional = citizensUtils.getShopId(event.getNPC().getId());
        if (shopIdOptional.isPresent()) {
            InventoryManager manager = plugin.getShopManager().getTradeManager(shopIdOptional.get());
            if (manager == null) {
                return;
            }
            if (player.hasPermission("neokeeper.admin") && player.isSneaking()) {
                manager.getEditGUI().openEditGUI(player);
            } else {
                manager.getTradeGUI().buildTradeGUI(player);
            }
            return;
        }

        Optional<String> stationIdOptional = citizensUtils.getStationId(event.getNPC().getId());
        stationIdOptional.ifPresent(stationId -> {
            StationDefinition station = plugin.getStationManager().getStation(stationId);
            if (station == null) {
                return;
            }
            if (player.hasPermission("neokeeper.admin") && player.isSneaking()) {
                station.getEditorListGUI().open(player, 0);
            } else {
                station.getPlayerListGUI().open(player, 0);
            }
        });
    }

    @EventHandler
    public void onNpcRemove(NPCRemoveEvent event) {
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (!citizensUtils.isAllowedCitizens()) {
            return;
        }

        int npcId = event.getNPC().getId();
        Optional<String> shopIdOptional = citizensUtils.getShopId(npcId);
        Optional<String> stationIdOptional = citizensUtils.getStationId(npcId);

        citizensUtils.unregisterShopkeeper(npcId);
        citizensUtils.unregisterStationNpc(npcId);

        shopIdOptional.ifPresent(shopId -> plugin.getShopManager().deleteShop(shopId, true));
        stationIdOptional.ifPresent(stationId -> plugin.getStationManager().deleteStation(stationId));
    }
}
