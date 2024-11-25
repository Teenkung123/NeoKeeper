package org.teenkung.neokeeper.Utils;

import org.bukkit.entity.Entity;
import org.teenkung.neokeeper.NeoKeeper;

public class CitizensUtils {

    private final NeoKeeper plugin;
    private boolean allowedCitizens = false;

    public CitizensUtils(NeoKeeper plugin) {
        this.plugin = plugin;
        if (plugin.getServer().getPluginManager().getPlugin("Citizens") != null) {
            allowedCitizens = plugin.getServer().getPluginManager().isPluginEnabled("Citizens");
        }
    }

    public boolean isNPC(Entity entity) {
        return entity.hasMetadata("NPC");
    }

    public boolean isAllowedCitizens() {
        return allowedCitizens;
    }

}
