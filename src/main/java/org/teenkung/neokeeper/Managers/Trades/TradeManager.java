package org.teenkung.neokeeper.Managers.Trades;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.inventory.ItemStack;
import org.teenkung.neokeeper.Managers.ItemManager;

public class TradeManager {

    private final ItemManager reward;
    private final ItemManager quest1;
    private final ItemManager quest2;

    public TradeManager(ConfigurationSection section) {
        reward = new ItemManager(section.getString("Reward.Type"), section.getString("Reward.Item"), section.getInt("Reward.Amount", 1));
        quest1 = new ItemManager(section.getString("Quests.1.Type", "NONE"), section.getString("Quests.1.Item", "NONE"), section.getInt("Quests.1.Amount", 0));
        quest2 = new ItemManager(section.getString("Quests.2.Type", "NONE"), section.getString("Quests.2.Item", "NONE"), section.getInt("Quests.2.Amount", 0));

    }

    public ItemStack getQuest1Item() {
        return quest1.getItem();
    }

    public ItemStack getQuest2Item() {
        return quest2.getItem();
    }

    public ItemStack getRewardItem() {
        return reward.getItem();
    }

    public ItemManager getQuest1Manager() { return quest1; }
    public ItemManager getQuest2Manager() { return quest2; }
    public ItemManager getRewardManager() { return reward; }

}
