package org.example1.timeSpeedPlugin;

import io.papermc.paper.event.player.AsyncChatEvent;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActivityTracker implements Listener {
    private static final Map<UUID, Long> LAST_ACTIVITY = new ConcurrentHashMap<>();
    private static final Map<UUID, Boolean> MANUAL_AFK = new ConcurrentHashMap<>();

    ActivityTracker() {}

    public static void onJoin(Player player) {
        UUID uuid = player.getUniqueId();
        LAST_ACTIVITY.put(uuid, System.currentTimeMillis());
    }

    public static void onQuit(Player player) {
        UUID uuid = player.getUniqueId();
        LAST_ACTIVITY.remove(uuid);
        MANUAL_AFK.remove(uuid);
    }

    public static void clearAll() {
        LAST_ACTIVITY.clear();
        MANUAL_AFK.clear();
    }

    public static void updateActivity(Player player) {
        LAST_ACTIVITY.put(player.getUniqueId(), System.currentTimeMillis());
    }

    public static boolean isAfk(Player player) {
        UUID uuid = player.getUniqueId();
        if (MANUAL_AFK.getOrDefault(uuid, false)) return true;
        if (!Config.ENABLE_AFK_SYSTEM) return false;
        long lastActive = LAST_ACTIVITY.getOrDefault(uuid, 0L);
        long timeoutMillis = (long) Config.AFK_TIMEOUT_MINUTES * 60 * 1000;
        return (System.currentTimeMillis() - lastActive) > timeoutMillis;
    }

    public static boolean isManualAfk(Player player) {
        return MANUAL_AFK.getOrDefault(player.getUniqueId(), false);
    }

    public static void setManualAfk(Player player, boolean afk) {
        MANUAL_AFK.put(player.getUniqueId(), afk);
        updateActivity(player);
    }

    @EventHandler(ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        Location from = event.getFrom();
        Location to = event.getTo();
        if (to == null) return;
        double dx = from.getX() - to.getX();
        double dy = from.getY() - to.getY();
        double dz = from.getZ() - to.getZ();
        if (dx * dx + dy * dy + dz * dz > 0.01
                || Math.abs(from.getYaw() - to.getYaw()) > 1.0f
                || Math.abs(from.getPitch() - to.getPitch()) > 1.0f) {
            updateActivity(event.getPlayer());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onCommand(PlayerCommandPreprocessEvent event) {
        updateActivity(event.getPlayer());
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        updateActivity(event.getPlayer());
    }
}
