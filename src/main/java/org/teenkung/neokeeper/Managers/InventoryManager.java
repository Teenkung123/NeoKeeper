package org.teenkung.neokeeper.Managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import net.kyori.adventure.text.Component;
import org.teenkung.neokeeper.GUIs.EditGUI;
import org.teenkung.neokeeper.GUIs.TradeGUI;
import org.teenkung.neokeeper.NeoKeeper;
import org.teenkung.neokeeper.Managers.Trades.TradeManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class InventoryManager {

    private final NeoKeeper plugin;
    private final YamlConfiguration config;
    private final String id;
    private final List<TradeManager> tradeManagers;
    private Component title;
    private TradeGUI tradeGUI;
    private EditGUI editGUI;

    public InventoryManager(NeoKeeper plugin, YamlConfiguration config, String id) {
        this.plugin = plugin;
        this.config = config;
        this.id = id;

        // Load TradeManagers from the configuration
        List<TradeManager> tempTradeManagers = new ArrayList<>();
        ConfigurationSection section = config.getConfigurationSection("Items");
        if (section != null) {
            for (String key : section.getKeys(false)) {
                tempTradeManagers.add(new TradeManager(Objects.requireNonNull(section.getConfigurationSection(key))));
            }
        }
        this.tradeManagers = Collections.unmodifiableList(tempTradeManagers);

        // Set the title from the configuration
        this.title = plugin.colorize(config.getString("Option.Title", "Default Shop"));
    }

    public String getId() {
        return id;
    }

    public Component getTitle() {
        return title;
    }

    public void setTitle(Component title) {
        config.set("Option.Title", title);
        this.title = title;
    }

    public List<TradeManager> getTradeManagers() {
        return tradeManagers;
    }

    public YamlConfiguration getConfig() {
        return config;
    }

    public NeoKeeper getPlugin() {
        return plugin;
    }

    // Methods to get instances of TradeGUI and EditGUI
    public TradeGUI getTradeGUI() {
        if (tradeGUI == null) {
            tradeGUI = new TradeGUI(this);
        }
        return tradeGUI;
    }

    public EditGUI getEditGUI() {
        if (editGUI == null) {
            editGUI = new EditGUI(this);
        }
        return editGUI;
    }
}
