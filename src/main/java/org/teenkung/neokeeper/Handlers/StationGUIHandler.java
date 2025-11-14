package org.teenkung.neokeeper.Handlers;

import de.tr7zw.nbtapi.NBT;
import de.tr7zw.nbtapi.iface.ReadableItemNBT;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.teenkung.neokeeper.GUIs.station.StationEditorListGUI;
import org.teenkung.neokeeper.GUIs.station.StationPlayerListGUI;
import org.teenkung.neokeeper.GUIs.station.StationPlayerRecipeGUI;
import org.teenkung.neokeeper.GUIs.station.StationRecipeEditorGUI;
import org.teenkung.neokeeper.Managers.ItemManager;
import org.teenkung.neokeeper.Managers.Stations.StationDefinition;
import org.teenkung.neokeeper.Managers.Stations.StationFeedbackConfig;
import org.teenkung.neokeeper.Managers.Stations.StationMaterialUtils;
import org.teenkung.neokeeper.Managers.Stations.StationRecipe;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Utils.CitizensUtils;
import org.teenkung.neokeeper.Utils.ItemComparison;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;

public class StationGUIHandler implements Listener {

    private final NeoKeeper plugin;
    private StationFeedbackConfig feedback() {
        return plugin.getStationFeedback();
    }
    private static final List<Integer> STORAGE_SLOTS = buildStorageSlots();

    public StationGUIHandler(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory top = event.getView().getTopInventory();
        InventoryHolder holder = top.getHolder();
        if (holder instanceof StationPlayerListGUI.PlayerListHolder playerListHolder) {
            handlePlayerList(event, playerListHolder);
        } else if (holder instanceof StationPlayerRecipeGUI.PlayerRecipeHolder recipeHolder) {
            handlePlayerRecipe(event, recipeHolder);
        } else if (holder instanceof StationEditorListGUI.EditorListHolder editorListHolder) {
            handleEditorList(event, editorListHolder);
        } else if (holder instanceof StationRecipeEditorGUI.EditorRecipeHolder editorRecipeHolder) {
            handleEditorRecipe(event, editorRecipeHolder);
        }
    }

    private void handlePlayerList(InventoryClickEvent event, StationPlayerListGUI.PlayerListHolder holder) {
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        StationDefinition station = plugin.getStationManager().getStation(holder.getStationId());
        if (station == null) {
            player.closeInventory();
            return;
        }
        if (current == null || current.getType().isAir()) {
            return;
        }
        String action = getAction(current);
        if (action != null) {
            switch (action) {
                case "NEXT" -> {
                    station.getPlayerListGUI().open(player, holder.getPage() + 1);
                    notifyGuiAction(player, "NEXT");
                }
                case "PREVIOUS" -> {
                    station.getPlayerListGUI().open(player, Math.max(holder.getPage() - 1, 0));
                    notifyGuiAction(player, "PREVIOUS");
                }
                case "BACK", "CLOSE" -> {
                    player.closeInventory();
                    notifyGuiAction(player, action);
                }
            }
            return;
        }
        if (hasRecipeTag(current)) {
            String recipeKey = getRecipeKey(current);
            StationRecipe recipe = station.getRecipe(recipeKey);
            if (recipe == null) {
                player.sendMessage(plugin.colorize("<red>Recipe not found."));
                return;
            }
            station.getPlayerRecipeGUI().open(player, recipe, holder.getPage());
        }
    }

    private void handlePlayerRecipe(InventoryClickEvent event, StationPlayerRecipeGUI.PlayerRecipeHolder holder) {
        if (event.getClickedInventory() == null || event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        StationDefinition station = plugin.getStationManager().getStation(holder.getStationId());
        if (station == null) {
            player.closeInventory();
            return;
        }
        if (current == null || current.getType().isAir()) {
            return;
        }
        StationPlayerRecipeGUI recipeGUI = station.getPlayerRecipeGUI();
        int resultSlot = recipeGUI.getResultSlot();
        String action = getAction(current);
        if (action != null) {
            if ("BACK".equalsIgnoreCase(action)) {
                station.getPlayerListGUI().open(player, holder.getPreviousPage());
                notifyGuiAction(player, "BACK");
            }
            return;
        }
        if (event.getSlot() == resultSlot && hasRecipeTag(current)) {
            String recipeKey = getRecipeKey(current);
            StationRecipe recipe = station.getRecipe(recipeKey);
            if (recipe == null) {
                player.sendMessage(plugin.colorize("<red>Recipe not found."));
                return;
            }
            attemptCraft(player, recipe);
        }
    }

    private void handleEditorList(InventoryClickEvent event, StationEditorListGUI.EditorListHolder holder) {
        if (event.getClickedInventory() != event.getView().getTopInventory()) {
            event.setCancelled(true);
            return;
        }
        event.setCancelled(true);
        ItemStack current = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        StationDefinition station = plugin.getStationManager().getStation(holder.getStationId());
        if (station == null) {
            player.closeInventory();
            return;
        }
        if (current == null || current.getType().isAir()) {
            return;
        }
        String action = getAction(current);
        if (action != null) {
            switch (action) {
                case "NEXT" -> {
                    int nextPage = clampEditorPage(station, holder.getPage() + 1);
                    if (nextPage != holder.getPage()) {
                        station.getEditorListGUI().open(player, nextPage);
                        notifyGuiAction(player, "NEXT");
                    }
                }
                case "PREVIOUS" -> {
                    int prevPage = clampEditorPage(station, holder.getPage() - 1);
                    if (prevPage != holder.getPage()) {
                        station.getEditorListGUI().open(player, prevPage);
                        notifyGuiAction(player, "PREVIOUS");
                    }
                }
                case "BACK", "CLOSE" -> {
                    player.closeInventory();
                    notifyGuiAction(player, action);
                }
                case "DELETE" -> {
                    deleteStation(player, station);
                    return;
                }
                case "ADD" -> {
                    String newKey = station.nextRecipeKey();
                    station.getRecipeEditorGUI().open(player, newKey, holder.getPage());
                    notifyGuiAction(player, "ADD");
                }
            }
            return;
        }

        if (hasRecipeTag(current)) {
            String recipeKey = getRecipeKey(current);
            boolean remove = event.getClick() == ClickType.SHIFT_RIGHT
                    || (event.isShiftClick() && event.getClick() == ClickType.RIGHT);
            if (remove) {
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.7F, 0.6F);
                deleteRecipe(player, station, recipeKey, holder.getPage());
                return;
            }
            station.getRecipeEditorGUI().open(player, recipeKey, holder.getPage());
        }
    }

    private void handleEditorRecipe(InventoryClickEvent event, StationRecipeEditorGUI.EditorRecipeHolder holder) {
        Inventory clicked = event.getClickedInventory();
        if (clicked == null) {
            return;
        }
        boolean topInventory = clicked == event.getView().getTopInventory();
        ItemStack current = event.getCurrentItem();
        Player player = (Player) event.getWhoClicked();
        StationDefinition station = plugin.getStationManager().getStation(holder.getStationId());
        if (station == null) {
            player.closeInventory();
            return;
        }

        if (!topInventory) {
            // allow normal behaviour for player inventory
            return;
        }

        int slot = event.getSlot();
        List<Integer> materialSlots = station.getRecipeEditorGUI().getMaterialSlots();
        int resultSlot = station.getRecipeEditorGUI().getResultSlot();
        String action = current != null ? getAction(current) : null;
        if (action != null) {
            event.setCancelled(true);
            switch (action) {
                case "BACK" -> {
                    station.getEditorListGUI().open(player, holder.getPreviousPage());
                    notifyGuiAction(player, "BACK");
                }
                case "SAVE" -> {
                    boolean saved = station.getRecipeEditorGUI().save(player, clicked, holder.getRecipeKey());
                    if (saved) {
                        station.getRecipeEditorGUI().open(player, holder.getRecipeKey(), holder.getPreviousPage());
                        notifyGuiAction(player, "SAVE");
                    }
                }
                case "DELETE" -> deleteRecipe(player, station, holder.getRecipeKey(), holder.getPreviousPage());
            }
            return;
        }

        if (!materialSlots.contains(slot) && slot != resultSlot) {
            event.setCancelled(true);
        } else if (slot == resultSlot && event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
            event.setCancelled(true);
        }
    }

    private void attemptCraft(Player player, StationRecipe recipe) {
        Map<Integer, Integer> removalPlan = planRemoval(player, recipe);
        if (removalPlan == null) {
            notifyTradeFail(player);
            return;
        }
        PlayerInventory inv = player.getInventory();
        for (Map.Entry<Integer, Integer> entry : removalPlan.entrySet()) {
            ItemStack stack = inv.getItem(entry.getKey());
            if (stack == null) {
                continue;
            }
            stack.setAmount(stack.getAmount() - entry.getValue());
            if (stack.getAmount() <= 0) {
                inv.setItem(entry.getKey(), null);
            }
        }

        ItemStack reward = recipe.getResultItem() != null ? recipe.getResultItem().clone() : null;
        if (reward == null) {
            player.sendMessage(plugin.colorize("<red>Recipe has no result item configured."));
            return;
        }
        Map<Integer, ItemStack> remaining = inv.addItem(reward);
        if (!remaining.isEmpty()) {
            remaining.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
        notifyTradeSuccess(player);
    }

    private Map<Integer, Integer> planRemoval(Player player, StationRecipe recipe) {
        PlayerInventory inv = player.getInventory();
        Map<Integer, Integer> removalPlan = new HashMap<>();
        List<StationMaterialUtils.AggregatedMaterial> requirements = StationMaterialUtils.aggregate(recipe.getMaterials());
        for (StationMaterialUtils.AggregatedMaterial aggregatedRequirement : requirements) {
            ItemManager requirement = aggregatedRequirement.template();
            int requiredAmount = aggregatedRequirement.amount();
            if (requirement == null || "NONE".equalsIgnoreCase(requirement.getType()) || requiredAmount <= 0) {
                continue;
            }
            int needed = requiredAmount;
            for (int slot : STORAGE_SLOTS) {
                ItemStack stack = inv.getItem(slot);
                if (stack == null || stack.getType().isAir()) {
                    continue;
                }
                if (!ItemComparison.matches(new ItemManager(stack), requirement)) {
                    continue;
                }
                int take = Math.min(needed, stack.getAmount());
                needed -= take;
                removalPlan.merge(slot, take, Integer::sum);
                if (needed <= 0) {
                    break;
                }
            }
            if (needed > 0) {
                return null;
            }
        }
        return removalPlan;
    }

    private static List<Integer> buildStorageSlots() {
        List<Integer> slots = new ArrayList<>();
        for (int i = 0; i < 36; i++) {
            slots.add(i);
        }
        slots.add(40); // offhand
        return slots;
    }

    private int clampEditorPage(StationDefinition station, int requestedPage) {
        int totalPages = Math.max(1, station.getEditorListGUI().getTotalPages());
        int maxPage = Math.max(0, totalPages - 1);
        if (requestedPage < 0) {
            return 0;
        }
        if (requestedPage > maxPage) {
            return maxPage;
        }
        return requestedPage;
    }

    private void notifyGuiAction(Player player, String action) {
        StationFeedbackConfig feedback = feedback();
        if (feedback != null && player != null) {
            feedback.notifyGuiAction(player, action);
        }
    }

    private void notifyTradeSuccess(Player player) {
        StationFeedbackConfig feedback = feedback();
        if (feedback != null && player != null) {
            feedback.notifyTradeSuccess(player);
        } else if (player != null) {
            player.sendMessage(plugin.colorize("<green>Successfully crafted the item."));
        }
    }

    private void notifyTradeFail(Player player) {
        StationFeedbackConfig feedback = feedback();
        if (feedback != null && player != null) {
            feedback.notifyTradeFail(player);
        } else if (player != null) {
            player.sendMessage(plugin.colorize("<red>You don't have the required materials."));
        }
    }

    private boolean hasRecipeTag(ItemStack item) {
        if (item == null) {
            return false;
        }
        return Boolean.TRUE.equals(NBT.get(item, (Function<ReadableItemNBT, Boolean>) nbt -> nbt.hasTag("NeoStationRecipe")));
    }

    private String getRecipeKey(ItemStack item) {
        if (item == null) {
            return null;
        }
        return NBT.get(item, (Function<ReadableItemNBT, String>) nbt -> nbt.getString("NeoStationRecipe"));
    }

    private String getAction(ItemStack item) {
        if (item == null) {
            return null;
        }
        Boolean hasAction = NBT.get(item, (Function<ReadableItemNBT, Boolean>) nbt -> nbt.hasTag("NeoStationAction"));
        if (!Boolean.TRUE.equals(hasAction)) {
            return null;
        }
        return NBT.get(item, (Function<ReadableItemNBT, String>) nbt -> {
            String action = nbt.getString("NeoStationAction");
            return action != null ? action.toUpperCase(Locale.ROOT) : null;
        });
    }

    private void deleteRecipe(Player player, StationDefinition station, String recipeKey, int fallbackPage) {
        if (recipeKey == null || recipeKey.isBlank()) {
            station.getEditorListGUI().open(player, clampEditorPage(station, fallbackPage));
            return;
        }
        StationRecipe recipe = station.getRecipe(recipeKey);
        if (recipe == null) {
            player.sendMessage(plugin.colorize("<yellow>No saved data found for this recipe."));
            station.getEditorListGUI().open(player, clampEditorPage(station, fallbackPage));
            return;
        }
        station.removeRecipe(recipeKey);
        notifyGuiAction(player, "DELETE");

        int pageToOpen = clampEditorPage(station, fallbackPage);
        station.getEditorListGUI().open(player, pageToOpen);
    }

    private void deleteStation(Player player, StationDefinition station) {
        if (station == null) {
            player.sendMessage(plugin.colorize("<red>Station is no longer available."));
            player.closeInventory();
            return;
        }
        CitizensUtils citizensUtils = plugin.getCitizensUtils();
        if (citizensUtils != null && citizensUtils.isAllowedCitizens()) {
            citizensUtils.removeStationNpc(station.getId());
        }
        boolean deleted = plugin.getStationManager().deleteStation(station.getId());
        if (deleted) {
            player.sendMessage(plugin.colorize("<green>Station " + station.getId() + " deleted."));
        } else {
            player.sendMessage(plugin.colorize("<red>Failed to delete station " + station.getId() + "."));
        }
        notifyGuiAction(player, "DELETE");
        player.closeInventory();
    }

}
