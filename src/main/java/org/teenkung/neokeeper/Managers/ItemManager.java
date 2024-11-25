package org.teenkung.neokeeper.Managers;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Utils.ItemStackSerialization;

import javax.annotation.Nullable;

/**
 * The {@code ItemManager} class is responsible for managing and manipulating {@link ItemStack} objects
 * within the NeoKeeper plugin. It supports different item types, including ItemsAdder (IA), MMOItems (MI),
 * and vanilla Minecraft items. This class facilitates serialization, deserialization, and comparison of items.
 */
public class ItemManager {

    /**
     * The type of the item. Possible values include "IA" for ItemsAdder, "MI" for MMOItems,
     * "VANILLA" for vanilla Minecraft items, and "NONE" for no item.
     */
    private String type;

    /**
     * The identifier for the item. This could be a namespaced ID for ItemsAdder, a combination
     * of type and ID for MMOItems, or a serialized string for vanilla items.
     */
    private String item;

    /**
     * A display string representation of the item. This is used for easy comparison and display purposes.
     */
    private final String itemDisplay;

    /**
     * The quantity of the item stack.
     */
    private Integer amount = 0;

    /**
     * Constructs a new {@code ItemManager} with the specified type, item identifier, and amount.
     *
     * @param type   the type of the item (e.g., "IA", "MI", "VANILLA")
     * @param item   the identifier of the item
     * @param amount the quantity of the item
     */
    public ItemManager(String type, String item, Integer amount) {
        this.type = type;
        this.item = item;
        this.amount = amount;

        if (this.type == null) { this.type = "NONE"; }
        if (this.item == null) { this.item = "NONE"; }
        if (this.type.equalsIgnoreCase("NONE") || this.item.equalsIgnoreCase("NONE")) { amount = 0; };

        this.itemDisplay = item;
    }

    /**
     * Constructs a new {@code ItemManager} based on the provided {@link ItemStack}.
     * Determines the item type and serializes the item accordingly.
     *
     * @param stack the {@code ItemStack} to be managed; can be {@code null}
     */
    public ItemManager(@Nullable ItemStack stack) {
        if (stack == null) {
            this.type = "NONE";
            this.item = "NONE";
            this.itemDisplay = "NONE";
            this.amount = 0;
            return;
        }
        this.amount = stack.getAmount();
        if (CustomStack.byItemStack(stack) != null) {
            this.type = "IA";
            this.item = CustomStack.byItemStack(stack).getNamespacedID();
            this.itemDisplay = item;
        } else if (NBTItem.get(stack).getType() != null) {
            this.type = "MI";
            NBTItem nbtItem = NBTItem.get(stack);
            this.item = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");
            this.itemDisplay = this.item;
        } else {
            this.type = "VANILLA";
            this.item = ItemStackSerialization.serialize(stack);
            ItemStack stackClone = stack.clone();
            stackClone.setAmount(1);
            this.itemDisplay = ItemStackSerialization.serialize(stackClone);
        }
    }

    /**
     * Retrieves the {@link ItemStack} represented by this {@code ItemManager}.
     * The returned {@code ItemStack} is constructed based on the item type and identifier.
     *
     * @return the corresponding {@code ItemStack}, or {@code null} if the type is "NONE"
     */
    public ItemStack getItem() {
        ItemStack returnStack;
        if (this.type.equalsIgnoreCase("IA")) {
            returnStack = CustomStack.getInstance(item).getItemStack();
        } else if (this.type.equalsIgnoreCase("MI")) {
            String[] args = item.split(":");
            String type = args[0];
            String id = args[1];
            returnStack = MMOItems.plugin.getItem(type, id);
        } else if (this.type.equalsIgnoreCase("NONE")) {
            returnStack = null;
        } else {
            returnStack = ItemStackSerialization.deserialize(item);
        }
        if (returnStack != null) {
            returnStack.setAmount(amount);
        }
        return returnStack;
    }

    /**
     * Retrieves a string representation of the item for comparison purposes.
     *
     * @return the display string of the item
     */
    public String getStringItem() { return itemDisplay; }

    /**
     * Retrieves the type of the item.
     *
     * @return the type of the item (e.g., "IA", "MI", "VANILLA", "NONE")
     */
    public String getType() { return type; }

    /**
     * Retrieves the quantity of the item stack.
     *
     * @return the amount of the item
     */
    public Integer getAmount() { return amount; }

}
