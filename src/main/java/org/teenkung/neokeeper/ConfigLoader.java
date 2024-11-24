package org.teenkung.neokeeper;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.teenkung.neokeeper.NeoKeeper;

import java.util.ArrayList;
import java.util.List;

public class ConfigLoader {
    private final FileConfiguration config;
    private final ArrayList<Integer> allSelectors = new ArrayList<>();

    public ConfigLoader(NeoKeeper plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        this.config = plugin.getConfig();

        List<String> selectors = getSelectorSlots();
        for (String selector : selectors) {
            String[] split = selector.split(":");
            for (String slot : split) {
                allSelectors.add(Integer.valueOf(slot));
            }
        }
    }

    public List<String> getGUILayout() { return config.getStringList("GUI.Layout"); }
    public ConfigurationSection getGUIItemsSection() { return config.getConfigurationSection("GUI.Items"); }
    public Integer getQuest1Slot() { return config.getInt("GUI.Slot.Quest1"); }
    public Integer getQuest2Slot() { return config.getInt("GUI.Slot.Quest2"); }
    public Integer getRewardSlot() { return config.getInt("GUI.Slot.Reward"); }
    public List<Integer> getNextPageSlots() { return config.getIntegerList("GUI.Slot.NextPage"); }
    public List<Integer> getPreviousPageSlots() { return config.getIntegerList("GUI.Slot.PreviousPage"); }
    public List<String> getSelectorSlots() { return config.getStringList("GUI.Slot.Lists"); }
    public ArrayList<Integer> getAllSelectors() { return allSelectors; }



}
