package org.teenkung.neokeeper.Managers;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.teenkung.neokeeper.Utils.ItemStackSerialization;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

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
     * The custom display name to be applied to the item.
     */
    private Component customDisplayName;

    /**
     * The custom lore to be applied to the item.
     */
    private List<Component> customLore;

    private boolean isDefaultName = true;

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
        this.amount = amount != null ? amount : 0;

        if (this.type == null) {
            this.type = "NONE";
        }
        if (this.item == null) {
            this.item = "NONE";
        }
        if (this.type.equalsIgnoreCase("NONE") || this.item.equalsIgnoreCase("NONE")) {
            this.amount = 0;
        }

        this.itemDisplay = item;
        if (type == null) return;
        if (type.equalsIgnoreCase("IA")) {
            CustomStack cStack = CustomStack.getInstance(item);
            if (cStack != null) {
                this.customDisplayName = cStack.getItemStack().asOne().displayName();
            }
        } else if (type.equalsIgnoreCase("MI")) {
            String[] args = item.split(":");
            if (args.length >= 2) {
                String type2 = args[0];
                String id = args[1];
                ItemStack cStack = MMOItems.plugin.getItem(type2, id);
                if (cStack != null) {
                    this.customDisplayName = cStack.asOne().displayName();
                }
            }
        } else {
            this.customDisplayName = null;
        }

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
            CustomStack cstack = CustomStack.byItemStack(stack);
            this.item = cstack.getNamespacedID();
            this.itemDisplay = item;
            this.customDisplayName = cstack.getItemStack().asOne().displayName();
            if (PlainTextComponentSerializer.plainText().serialize(this.customDisplayName) == PlainTextComponentSerializer.plainText().serialize(stack.asOne().displayName())) {
                this.isDefaultName = true;
            }
        } else if (NBTItem.get(stack).getType() != null) {
            this.type = "MI";
            NBTItem nbtItem = NBTItem.get(stack);
            this.item = nbtItem.getType() + ":" + nbtItem.getString("MMOITEMS_ITEM_ID");
            ItemStack cstack = MMOItems.plugin.getItem(nbtItem.getType(), nbtItem.getString("MMOITEMS_ITEM_ID"));
            this.customDisplayName = cstack.asOne().displayName();
            this.itemDisplay = this.item;
            if (PlainTextComponentSerializer.plainText().serialize(this.customDisplayName) == PlainTextComponentSerializer.plainText().serialize(stack.asOne().displayName())) {
                this.isDefaultName = true;
            }
        } else {
            this.type = "VANILLA";
            this.item = ItemStackSerialization.serialize(stack);
            ItemStack stackClone = stack.clone();
            stackClone.setAmount(1);
            this.itemDisplay = ItemStackSerialization.serialize(stackClone);
        }
    }

    /**
     * Generates and retrieves the {@link ItemStack} represented by this {@code ItemManager}.
     * The returned {@code ItemStack} is constructed based on the item type and identifier,
     * with any custom display name or lore applied.
     *
     * @return the corresponding {@code ItemStack}, or {@code null} if the type is "NONE"
     */
    public ItemStack getItem() {
        ItemStack returnStack = null;
        if (this.type.equalsIgnoreCase("IA")) {
            CustomStack customStack = CustomStack.getInstance(item);
            if (customStack != null) {
                returnStack = customStack.getItemStack();
            }
        } else if (this.type.equalsIgnoreCase("MI")) {
            String[] args = item.split(":");
            if (args.length >= 2) {
                String type = args[0];
                String id = args[1];
                returnStack = MMOItems.plugin.getItem(type, id);
            }
        } else if (this.type.equalsIgnoreCase("VANILLA")) {
            returnStack = ItemStackSerialization.deserialize(item);
        }

        if (returnStack != null) {
            returnStack.setAmount(amount);
            ItemMeta meta = returnStack.getItemMeta();
            if (meta != null) {
                if (customDisplayName != null && !isDefaultName) {
                    meta.displayName(customDisplayName.decorationIfAbsent(TextDecoration.ITALIC, TextDecoration.State.FALSE));
                }
                if (customLore != null) {
                    meta.lore(customLore);
                }
                returnStack.setItemMeta(meta);
            }
        }
        return returnStack;
    }

    /**
     * Sets the display name of the item using the Adventure Component API.
     *
     * @param displayName the display name to set as a {@link Component}
     */
    public void setDisplayName(Component displayName) {
        this.customDisplayName = displayName;
    }

    /**
     * Gets the custom display name set for the item as a {@link Component}.
     *
     * @return the custom display name of the item, or {@code null} if not set
     */
    @Nullable
    public Component getDisplayName() {
        if (this.type.equals("IA") && customDisplayName == null) {
            return CustomStack.byItemStack(getItem()).getItemStack().asOne().displayName();
        }
        if (this.type.equals("MI") && customDisplayName == null) {
            //noinspection DataFlowIssue
            return MMOItems.plugin.getItem(item.split(":")[0], item.split(":")[1]).asOne().displayName();
        }
        return customDisplayName;
    }

    /**
     * Sets the lore of the item using the Adventure Component API.
     *
     * @param lore a list of {@link Component} representing the lore lines
     */
    public void setLore(List<Component> lore) {
        this.customLore = lore;
    }

    /**
     * Gets the custom lore set for the item as a list of {@link Component}.
     *
     * @return the custom lore of the item, or {@code null} if not set
     */
    @Nullable
    public List<Component> getLore() {
        return customLore;
    }

    /**
     * Adds a line to the item's lore.
     *
     * @param line the {@link Component} line to add
     */
    public void addLoreLine(Component line) {
        if (this.customLore == null) {
            this.customLore = new ArrayList<>();
        }
        this.customLore.add(line);
    }

    /**
     * Clears the lore of the item.
     */
    public void clearLore() {
        if (this.customLore != null) {
            this.customLore.clear();
        }
    }

    /**
     * Retrieves a string representation of the item for comparison purposes.
     *
     * @return the display string of the item
     */
    public String getStringItem() {
        return itemDisplay;
    }

    /**
     * Retrieves the type of the item.
     *
     * @return the type of the item (e.g., "IA", "MI", "VANILLA", "NONE")
     */
    public String getType() {
        return type;
    }

    /**
     * Retrieves the quantity of the item stack.
     *
     * @return the amount of the item
     */
    public Integer getAmount() {
        return amount;
    }

    /**
     * Sets the quantity of the item stack.
     *
     * @param amount the new amount to set
     */
    public void setAmount(int amount) {
        this.amount = amount;
    }

    /**
     * Checks if the item has a custom display name.
     *
     * @return {@code true} if the item has a custom display name, {@code false} otherwise
     */
    public boolean hasDisplayName() {
        return customDisplayName != null;
    }

    /**
     * Checks if the item has custom lore.
     *
     * @return {@code true} if the item has custom lore, {@code false} otherwise
     */
    public boolean hasLore() {
        return customLore != null && !customLore.isEmpty();
    }
}
