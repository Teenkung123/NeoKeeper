package org.teenkung.neokeeper.Managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

public class TradeManager {

    private final ConfigurationSection section;
    private ItemManager reward;
    private ItemManager quest1;
    private ItemManager quest2;

    public TradeManager(ConfigurationSection section) {
        this.section = section;
    }

}
