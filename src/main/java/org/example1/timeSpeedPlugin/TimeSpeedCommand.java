package org.example1.timeSpeedPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimeSpeedCommand implements CommandExecutor, TabCompleter {

    private static final long SUNRISE = 23000;
    private static final long DAY = 1000;
    private static final long NOON = 6000;
    private static final long SUNSET = 12000;
    private static final long NIGHT = 13000;
    private static final long MIDNIGHT = 18000;
    private static final String[] PRESETS = {"sunrise", "day", "noon", "sunset", "night", "midnight"};

    private static final Map<UUID, BukkitTask> TASKS = new ConcurrentHashMap<>();

    private final TimeSpeedPlugin plugin;

    public TimeSpeedCommand(TimeSpeedPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Активна ли сейчас ручная перемотка времени в этом мире.
     * Используется SleepHandler, чтобы не конфликтовать с таймером сна.
     */
    public static boolean isRewinding(World world) {
        return TASKS.containsKey(world.getUID());
    }

    /** Actual time multiplier while a manual rewind is active, or zero otherwise. */
    public static double getRewindSpeed(World world) {
        return isRewinding(world) ? Math.max(1.0, Config.FAST_TIME_SPEED) : 0.0;
    }

    public static void stopAll() {
        for (BukkitTask task : TASKS.values()) {
            task.cancel();
        }
        TASKS.clear();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text(Lang.getConsole("only-players"), NamedTextColor.RED));
            return true;
        }
        if (args.length == 0) {
            player.sendMessage(Component.text(Lang.get(player, "timespeed-usage"), NamedTextColor.RED));
            return true;
        }

        long target = parseTime(args[0]);
        if (target < 0) {
            player.sendMessage(Component.text(Lang.get(player, "timespeed-unknown"), NamedTextColor.RED));
            return true;
        }
        target %= 24000;

        World world = player.getWorld();
        long start = world.getTime() % 24000;
        long distance = (target - start + 24000) % 24000;
        if (distance == 0) distance = 24000;

        long startFull = world.getFullTime();
        long endFull = startFull + distance;

        BukkitTask old = TASKS.remove(world.getUID());
        if (old != null) old.cancel();

        String name = timeName(player, target);
        player.sendMessage(Component.text(Lang.get(player, "timespeed-start"), NamedTextColor.GOLD));
        player.sendMessage(Component.text(Lang.get(player, "timespeed-target"), NamedTextColor.GRAY)
                .append(Component.text(name + " (" + target + ")", NamedTextColor.YELLOW)));

        final long fTarget = target;
        BukkitTask task = Bukkit.getScheduler().runTaskTimer(plugin,
                () -> tick(world, startFull, endFull, fTarget), 1L, 1L);
        TASKS.put(world.getUID(), task);
        return true;
    }

    private void tick(World world, long startFull, long endFull, long target) {
        try {
            tickRewind(world, startFull, endFull, target);
        } catch (Exception e) {
            BukkitTask task = TASKS.remove(world.getUID());
            if (task != null) task.cancel();
            plugin.getLogger().severe("Time rewind failed: " + e);
        }
    }

    private void tickRewind(World world, long startFull, long endFull, long target) {
        long currentFull = world.getFullTime();
        long speed = Math.max(1, (long) Config.FAST_TIME_SPEED);
        long remaining = endFull - currentFull;

        if (remaining <= speed) {
            world.setFullTime(endFull);
            BukkitTask task = TASKS.remove(world.getUID());
            if (task != null) task.cancel();
            for (Player p : world.getPlayers()) {
                if (p.hasPermission("timespeed.time")) {
                    Component msg = Component.text(Lang.get(p, "timespeed-done"), NamedTextColor.YELLOW)
                            .append(Component.text(timeName(p, target) + " (" + target + ")", NamedTextColor.GOLD));
                    p.sendMessage(msg);
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f, 0.8f);
                }
            }
            return;
        }

        world.setFullTime(currentFull + speed);
        currentFull += speed;
        remaining = endFull - currentFull;

        long total = endFull - startFull;
        float frac = Math.clamp((float) (currentFull - startFull) / total, 0f, 1f);
        long secondsLeft = Math.max(1, remaining / (speed * 20));
        int filled = Math.min(10, (int) (frac * 10));
        String bar = "▓".repeat(filled) + "░".repeat(10 - filled);

        NamedTextColor barColor = frac < 0.5f ? NamedTextColor.DARK_AQUA : (frac < 0.85f ? NamedTextColor.AQUA : NamedTextColor.GREEN);
        for (Player p : world.getPlayers()) {
            if (!p.hasPermission("timespeed.time")) continue;
            String msg = "⏩ " + bar + " " + secondsLeft + " " + Lang.get(p, "sec");
            ActionBarCompat.show(plugin, p, "timespeed:rewind", Component.text(msg, barColor), 3, 100);
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length != 1) return List.of();
        String prefix = args[0].toLowerCase();
        List<String> result = new ArrayList<>();
        for (String preset : PRESETS) {
            if (preset.startsWith(prefix)) result.add(preset);
        }
        return result;
    }

    private static long parseTime(String arg) {
        switch (arg.toLowerCase()) {
            case "sunrise": return SUNRISE;
            case "day": return DAY;
            case "noon": return NOON;
            case "sunset": return SUNSET;
            case "night": return NIGHT;
            case "midnight": return MIDNIGHT;
        }
        try {
            long v = Long.parseLong(arg);
            if (v < 0 || v > 24000) return -1;
            return v;
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private static String timeName(Player player, long time) {
        return switch ((int) time) {
            case 23000 -> Lang.get(player, "time-sunrise");
            case 1000 -> Lang.get(player, "time-day");
            case 6000 -> Lang.get(player, "time-noon");
            case 12000 -> Lang.get(player, "time-sunset");
            case 13000 -> Lang.get(player, "time-night");
            case 18000 -> Lang.get(player, "time-midnight");
            default -> Lang.get(player, "time-unknown", time);
        };
    }
}
