package org.example1.timeSpeedPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class AfkCommand implements CommandExecutor, TabCompleter {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(Lang.getConsole("only-players"), NamedTextColor.RED));
            return true;
        }
        if (!Config.ENABLE_MANUAL_AFK) {
            player.sendMessage(Component.text(Lang.get(player, "afk-disabled"), NamedTextColor.RED));
            return true;
        }

        boolean afk = !ActivityTracker.isManualAfk(player);
        ActivityTracker.setManualAfk(player, afk);
        player.sendMessage(Component.text(afk ? Lang.get(player, "afk-on") : Lang.get(player, "afk-off"), NamedTextColor.GRAY));
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}
