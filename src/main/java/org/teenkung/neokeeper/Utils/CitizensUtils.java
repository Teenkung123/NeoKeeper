package org.teenkung.neokeeper.Utils;

import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.npc.NPC;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class CitizensUtils {

    private static final String SHOP_DATA_KEY = "neokeeper.shop-id";
    private static final String STATION_DATA_KEY = "neokeeper.station-id";

    private final NeoKeeper plugin;
    private final Map<Integer, String> npcToShop = new HashMap<>();
    private final Map<String, Integer> shopToNpc = new HashMap<>();
    private final Map<Integer, String> npcToStation = new HashMap<>();
    private final Map<String, Integer> stationToNpc = new HashMap<>();
    private boolean allowedCitizens = false;

    public CitizensUtils(NeoKeeper plugin) {
        this.plugin = plugin;
        try {
            Class.forName("net.citizensnpcs.api.CitizensAPI");
        } catch (Throwable ignored) {
            plugin.getLogger().info("Citizens plugin not found. NPC integration disabled.");
            return;
        }

        if (plugin.getServer().getPluginManager().getPlugin("Citizens") != null &&
                plugin.getServer().getPluginManager().isPluginEnabled("Citizens")) {
            this.allowedCitizens = true;
            loadExistingShopkeepers();
        }
    }

    private void loadExistingShopkeepers() {
        try {
            for (NPC npc : CitizensAPI.getNPCRegistry()) {
                if (!npc.data().has(SHOP_DATA_KEY)) {
                } else {
                    String shopId = npc.data().get(SHOP_DATA_KEY);
                    if (shopId != null && !shopId.isEmpty()) {
                        npcToShop.put(npc.getId(), shopId);
                        shopToNpc.put(shopId, npc.getId());
                    }
                }
                if (npc.data().has(STATION_DATA_KEY)) {
                    String stationId = npc.data().get(STATION_DATA_KEY);
                    if (stationId != null && !stationId.isEmpty()) {
                        npcToStation.put(npc.getId(), stationId);
                        stationToNpc.put(stationId, npc.getId());
                    }
                }
            }
        } catch (Exception exception) {
            plugin.getLogger().warning("Failed to load Citizens NPC bindings: " + exception.getMessage());
        }
    }

    public boolean isNPC(Entity entity) {
        return entity.hasMetadata("NPC");
    }

    public boolean isAllowedCitizens() {
        return allowedCitizens;
    }

    public Optional<String> getShopId(int npcId) {
        if (!npcToShop.containsKey(npcId) && allowedCitizens) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
            if (npc != null && npc.data().has(SHOP_DATA_KEY)) {
                String shopId = npc.data().get(SHOP_DATA_KEY);
                if (shopId != null) {
                    npcToShop.put(npcId, shopId);
                    shopToNpc.put(shopId, npcId);
                }
            }
        }
        return Optional.ofNullable(npcToShop.get(npcId));
    }

    public Optional<Integer> getNpcIdForShop(String shopId) {
        return Optional.ofNullable(shopToNpc.get(shopId));
    }

    public Optional<String> getStationId(int npcId) {
        if (!npcToStation.containsKey(npcId) && allowedCitizens) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
            if (npc != null && npc.data().has(STATION_DATA_KEY)) {
                String stationId = npc.data().get(STATION_DATA_KEY);
                if (stationId != null) {
                    npcToStation.put(npcId, stationId);
                    stationToNpc.put(stationId, npcId);
                }
            }
        }
        return Optional.ofNullable(npcToStation.get(npcId));
    }

    public Optional<Integer> getNpcIdForStation(String stationId) {
        return Optional.ofNullable(stationToNpc.get(stationId));
    }

    public boolean isShopkeeper(NPC npc) {
        return npc != null && npcToShop.containsKey(npc.getId());
    }

    public boolean bindExistingShopkeeper(NPC npc, String shopId) {
        if (!allowedCitizens || npc == null || shopId == null || shopId.isEmpty()) {
            return false;
        }
        registerShopkeeper(npc, shopId);
        return true;
    }

    public boolean bindExistingStationNpc(NPC npc, String stationId) {
        if (!allowedCitizens || npc == null || stationId == null || stationId.isEmpty()) {
            return false;
        }
        registerStationNpc(npc, stationId);
        return true;
    }

    public ShopkeeperCreationResult createShopkeeperNPC(Player player, String displayName, String shopId) {
        if (!allowedCitizens) {
            return null;
        }
        try {
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, displayName);
            npc.setName(displayName);
            npc.setProtected(true);
            registerShopkeeper(npc, shopId);
            npc.spawn(player.getLocation());
            return new ShopkeeperCreationResult(npc.getId(), npc.getName());
        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to create shopkeeper NPC: " + exception.getMessage());
            return null;
        }
    }

    public StationCreationResult createStationNPC(Player player, String displayName, String stationId) {
        if (!allowedCitizens) {
            return null;
        }
        try {
            NPC npc = CitizensAPI.getNPCRegistry().createNPC(EntityType.VILLAGER, displayName);
            npc.setName(displayName);
            npc.setProtected(true);
            registerStationNpc(npc, stationId);
            npc.spawn(player.getLocation());
            return new StationCreationResult(npc.getId(), npc.getName());
        } catch (Exception exception) {
            plugin.getLogger().severe("Failed to create station NPC: " + exception.getMessage());
            return null;
        }
    }

    private void registerShopkeeper(NPC npc, String shopId) {
        if (npc == null || shopId == null || shopId.isEmpty()) {
            return;
        }

        String existingShopForNpc = npcToShop.get(npc.getId());
        if (existingShopForNpc != null && !existingShopForNpc.equalsIgnoreCase(shopId)) {
            shopToNpc.remove(existingShopForNpc);
        }

        Integer existingNpcForShop = shopToNpc.get(shopId);
        if (existingNpcForShop != null && existingNpcForShop != npc.getId()) {
            npcToShop.remove(existingNpcForShop);
            NPC previousNpc = CitizensAPI.getNPCRegistry().getById(existingNpcForShop);
            if (previousNpc != null) {
                previousNpc.data().remove(SHOP_DATA_KEY);
            }
        }

        npc.data().setPersistent(SHOP_DATA_KEY, shopId);
        npcToShop.put(npc.getId(), shopId);
        shopToNpc.put(shopId, npc.getId());
    }

    private void registerStationNpc(NPC npc, String stationId) {
        if (npc == null || stationId == null || stationId.isEmpty()) {
            return;
        }

        String existingStation = npcToStation.get(npc.getId());
        if (existingStation != null && !existingStation.equalsIgnoreCase(stationId)) {
            stationToNpc.remove(existingStation);
        }

        Integer existingNpc = stationToNpc.get(stationId);
        if (existingNpc != null && existingNpc != npc.getId()) {
            npcToStation.remove(existingNpc);
            NPC previousNpc = CitizensAPI.getNPCRegistry().getById(existingNpc);
            if (previousNpc != null) {
                previousNpc.data().remove(STATION_DATA_KEY);
            }
        }

        npc.data().setPersistent(STATION_DATA_KEY, stationId);
        npcToStation.put(npc.getId(), stationId);
        stationToNpc.put(stationId, npc.getId());
    }

    public void unregisterShopkeeper(int npcId) {
        String shopId = npcToShop.remove(npcId);
        if (shopId != null) {
            shopToNpc.remove(shopId);
        }
        if (allowedCitizens) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
            if (npc != null) {
                npc.data().remove(SHOP_DATA_KEY);
            }
        }
    }

    public void unregisterStationNpc(int npcId) {
        String stationId = npcToStation.remove(npcId);
        if (stationId != null) {
            stationToNpc.remove(stationId);
        }
        if (allowedCitizens) {
            NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
            if (npc != null) {
                npc.data().remove(STATION_DATA_KEY);
            }
        }
    }

    public boolean removeShopkeeper(String shopId) {
        if (!allowedCitizens) {
            return false;
        }
        Integer npcId = shopToNpc.remove(shopId);
        if (npcId == null) {
            return false;
        }
        npcToShop.remove(npcId);
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
        if (npc == null) {
            return false;
        }
        npc.data().remove(SHOP_DATA_KEY);
        npc.destroy();
        return true;
    }

    public boolean removeStationNpc(String stationId) {
        if (!allowedCitizens) {
            return false;
        }
        Integer npcId = stationToNpc.remove(stationId);
        if (npcId == null) {
            return false;
        }
        npcToStation.remove(npcId);
        NPC npc = CitizensAPI.getNPCRegistry().getById(npcId);
        if (npc == null) {
            return false;
        }
        npc.data().remove(STATION_DATA_KEY);
        npc.destroy();
        return true;
    }

    public record ShopkeeperCreationResult(int npcId, String name) {}
    public record StationCreationResult(int npcId, String name) {}
}
