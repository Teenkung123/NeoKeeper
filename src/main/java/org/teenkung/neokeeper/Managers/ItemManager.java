package org.teenkung.neokeeper.Managers;

import dev.lone.itemsadder.api.CustomStack;
import io.lumine.mythic.lib.api.item.NBTItem;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.ItemStackSerialization;

public class ItemManager {

    private final String type;
    private final String item;
    private final Integer amount;


    public ItemManager(String type, String item, Integer amount) {
        this.type = type;
        this.item = item;
        this.amount = amount;
    }

    public ItemManager(ItemStack stack) {
        this.amount = stack.getAmount();
        if (CustomStack.byItemStack(stack) != null) {
            this.type = "IA";
            this.item = CustomStack.byItemStack(stack).getNamespacedID();
        } else if (NBTItem.get(stack).getType() != null) {
            this.type = "MI";
            NBTItem nbtItem = NBTItem.get(stack);
            this.item = nbtItem.getType()+":"+nbtItem.getString("MMOITEMS_ITEM_ID");
        } else {
            this.type = "VANILLA";
            this.item = ItemStackSerialization.serialize(stack);
        }
    }

    public ItemStack getItem() {
        ItemStack returnStack;
        if (this.type.equals("IA")) {
            returnStack = CustomStack.getInstance(item).getItemStack();
        } else if (this.type.equals("MI")) {
            String[] args = item.split(":");
            String type = args[0];
            String id = args[1];
            returnStack = MMOItems.plugin.getItem(type, id);
        } else {
            returnStack = ItemStackSerialization.deserialize(item);
        }
        if (returnStack != null) {
            returnStack.setAmount(amount);
        }
        return returnStack;
    }

    protected String getStringItem() { return item; }
    protected String getType() { return type; }


}
