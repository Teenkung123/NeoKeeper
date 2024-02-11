package org.teenkung.neokeeper.Managers;

import com.google.inject.internal.Nullable;
import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.ItemStackSerialization;

public class ItemManager {

    private String type;
    private String item;
    private String itemDisplay;
    private Integer amount;


    public ItemManager(String type, String item, Integer amount) {
        this.type = type;
        this.item = item;
        this.amount = amount;

        if (this.type == null) { this.type = "NONE"; }
        if (this.item == null) { this.item = "NONE"; }
        if (this.type.equalsIgnoreCase("NONE") || this.item.equalsIgnoreCase("NONE")) { amount = 0; };

        this.itemDisplay = item;
    }

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
            this.item = nbtItem.getType()+":"+nbtItem.getString("MMOITEMS_ITEM_ID");
            this.itemDisplay = this.item;
        } else {
            this.type = "VANILLA";
            this.item = ItemStackSerialization.serialize(stack);
            ItemStack stackClone = stack.clone();
            stackClone.setAmount(1);
            this.itemDisplay = ItemStackSerialization.serialize(stackClone);
        }
    }

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

    public String getStringItem() { return itemDisplay; }
    public String getType() { return type; }
    public Integer getAmount() { return amount; }


}
