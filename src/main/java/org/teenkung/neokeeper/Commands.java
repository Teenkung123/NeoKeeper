package org.teenkung.neokeeper;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class Commands implements CommandExecutor {

    private final NeoKeeper plugin;
    public Commands(NeoKeeper plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        sender.sendMessage(plugin.getShopLoader().getAllShopManagers().toString());
        plugin.getShopLoader().getShopManager(
                plugin.getShopLoader().getAllShopManagers().keySet().iterator().next()
        ).buildGUI((Player) sender);
        return false;
    }
}
