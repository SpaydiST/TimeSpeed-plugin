package org.example1.timeSpeedPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRules;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.World.Environment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.TimeSkipEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class SleepHandler implements Listener {
    private static final String CURSED_RING_PDC_KEY = "cursed_ring";
    private static final long CLEAR_MORNING_TICK = 23450L;
    private static final long STORM_MORNING_TICK = 23900L;

    private final TimeSpeedPlugin plugin;
    private final NamespacedKey cursedRingKey;
    private final Map<UUID, WorldSleepState> worldStates = new ConcurrentHashMap<>();

    public SleepHandler(TimeSpeedPlugin plugin) {
        this.plugin = plugin;
        this.cursedRingKey = new NamespacedKey(plugin, CURSED_RING_PDC_KEY);
    }

    public void start() {
        plugin.getServer().getScheduler().runTaskTimer(plugin, this::tick, 20L, 1L);
    }

    public void stop() {
        plugin.getServer().getScheduler().cancelTasks(plugin);
        worldStates.clear();
    }

    /**
     * Текущая фактическая скорость перемотки времени в мире (0 — перемотка не активна,
     * 1 — обычная скорость, больше 1 — ускоренная перемотка сна/грозы).
     * Используется другими плагинами (например, HUD часов).
     */
    public double getWorldSpeed(World world) {
        if (!Config.ENABLE_SLEEP_SYSTEM) return 0.0;
        WorldSleepState st = worldStates.get(world.getUID());
        return st == null ? 0.0 : st.currentSpeed;
    }

    @EventHandler(ignoreCancelled = true)
    public void onTimeSkip(TimeSkipEvent event) {
        if (event.getSkipReason() != TimeSkipEvent.SkipReason.NIGHT_SKIP) return;
        if (!Config.ENABLE_SLEEP_SYSTEM) return;
        if (event.getWorld().getEnvironment() != Environment.NORMAL) return;
        event.setCancelled(true);
    }

    private void tick() {
        if (!Config.ENABLE_SLEEP_SYSTEM) return;
        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() != Environment.NORMAL) continue;
            processWorld(world);
        }
    }

    private final java.util.Set<UUID> daylightWarned = ConcurrentHashMap.newKeySet();

    private void processWorld(World world) {
        // Натуральный ход времени (advance_time, бывший doDaylightCycle) может быть
        // заморожен админом или сторонним софтом — это не повод отключать перемотку сна:
        // мы двигаем время явно через World.setTime(), как и команда /timespeed.
        if (!Boolean.TRUE.equals(world.getGameRuleValue(GameRules.ADVANCE_TIME))
                && daylightWarned.add(world.getUID())) {
            plugin.getLogger().info("В мире " + world.getName()
                    + " выключен натуральный ход времени (advance_time=false); перемотка сна продолжит работать");
        }
        List<Player> players = world.getPlayers();
        if (players.isEmpty()) {
            worldStates.remove(world.getUID());
            return;
        }

        WorldSleepState st = worldStates.computeIfAbsent(world.getUID(), k -> new WorldSleepState());

        // Пока активна ручная перемотка /timespeed, обработка сна ставится на паузу,
        // чтобы два таймера не дрались за world.setTime(). Спящих не трогаем;
        // serverSpeed сохраняется, чтобы после команды сработала логика «утра»,
        // если перемотка закончилась уже за пределами ночи.
        boolean paused = TimeSpeedCommand.isRewinding(world);
        if (paused) {
            st.currentSpeed = 0f;
            st.timeAccumulator = 0f;
            return;
        }

        long timeOfDay = world.getTime() % 24000;
        long morningTick = (world.hasStorm() || world.isThundering()) ? STORM_MORNING_TICK : CLEAR_MORNING_TICK;
        boolean isNightWindow = timeOfDay >= 12000 && timeOfDay < morningTick;
        boolean canSleepNow = isNightWindow
                || (Config.ENABLE_THUNDER_SKIP && world.isThundering() && timeOfDay < morningTick);

        if (!canSleepNow && st.serverSpeed != 0) {
            finishMorning(world, players, st);
            return;
        }

        long totalValidPlayers = 0;
        long sleepingPlayers = 0;
        for (Player p : players) {
            if (p.getGameMode() != GameMode.SPECTATOR && !ActivityTracker.isAfk(p)) {
                totalValidPlayers++;
                if (p.isSleeping() && !hasCursedRing(p)) {
                    sleepingPlayers++;
                }
            }
        }
        totalValidPlayers = Math.max(1, totalValidPlayers);

        float maxSpeed = (float) Config.MAX_SLEEP_SPEED;
        float target = 0f;
        if (sleepingPlayers > 0 && canSleepNow && totalValidPlayers > 0) {
            int percent = world.getGameRuleValue(GameRules.PLAYERS_SLEEPING_PERCENTAGE);
            int playersNeeded = Math.max(1, Math.round(totalValidPlayers * percent / 100.0f));
            float ratio = (float) sleepingPlayers / playersNeeded;
            if (ratio >= 1.0f) {
                target = maxSpeed;
            } else {
                target = (float) Math.min(maxSpeed, 1.0 + ratio / (1.0 - ratio));
            }
            if (target < 1.0f) target = 1.0f;
        }

        float speed;
        if (target > 0f) {
            float lerp = (float) Config.SPEED_LERP_FACTOR;
            st.currentSpeed += (target - st.currentSpeed) * lerp;
            if (Math.abs(target - st.currentSpeed) < 0.01f) st.currentSpeed = target;
            if (st.currentSpeed < 1.0f) st.currentSpeed = 1.0f;
            speed = st.currentSpeed;
        } else {
            st.currentSpeed = 0f;
            speed = 0f;
        }

        if (speed > 1.0f) {
            if (!st.rewinding) {
                st.rewinding = true;
                st.rewindStart = timeOfDay;
            }
            st.timeAccumulator += (speed - 1.0f);
            int ticks = (int) st.timeAccumulator;
            st.serverSpeed = (long) speed;

            if (ticks > 0) {
                st.timeAccumulator -= ticks;
                if (timeOfDay >= 12000 && timeOfDay + ticks >= morningTick) {
                    world.setTime(world.getTime() + (24000 - timeOfDay));
                    finishMorning(world, players, st);
                    return;
                }
                world.setTime(world.getTime() + ticks);
                timeOfDay = world.getTime() % 24000;
            }

            if (timeOfDay < morningTick) {
                long remaining = Math.max(0L, morningTick - timeOfDay);
                long secondsLeft = Math.max(1L, (long) (remaining / (Math.max(1.0f, speed) * 20L)));
                float frac = 0f;
                long rewindLen = morningTick - st.rewindStart;
                if (rewindLen > 0) {
                    frac = Math.clamp((float) (timeOfDay - st.rewindStart) / rewindLen, 0f, 1f);
                }
                int filled = Math.min(10, (int) (frac * 10));
                String bar = "▓".repeat(filled) + "░".repeat(10 - filled);
                NamedTextColor barColor = frac < 0.5f ? NamedTextColor.DARK_AQUA : (frac < 0.85f ? NamedTextColor.AQUA : NamedTextColor.GREEN);
                for (Player p : players) {
                    if (p.isSleeping()) {
                        String msg = "🌙 " + bar + " " + secondsLeft + " " + Lang.get(p, "sec");
                        ActionBarCompat.show(plugin, p, "timespeed:sleep", Component.text(msg, barColor), 3, 100);
                    }
                }
            }
        } else if (st.serverSpeed != 0) {
            resetState(st);
        }
    }

    private static void finishMorning(World world, List<Player> players, WorldSleepState st) {
        resetState(st);
        boolean endedStorm = world.isThundering() || world.hasStorm();
        if (endedStorm) {
            world.setStorm(false);
            world.setThundering(false);
            world.setWeatherDuration(6000);
        }
        for (Player p : players) {
            p.sendMessage(Component.text(Lang.get(p, "morning"), NamedTextColor.YELLOW));
            world.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.MASTER, 0.5f, 0.8f);
            if (p.isSleeping()) {
                ProgressionAdvancement.award(p);
                p.wakeup(false);
            }
            if (Config.RESET_PHANTOMS_FOR_ALL) p.setStatistic(org.bukkit.Statistic.TIME_SINCE_REST, 0);
        }
    }

    /**
     * Проверка «проклятого кольца» без создания мусора:
     * материалы разбираются один раз при загрузке конфига (Config.CURSED_RING_MATERIALS),
     * сравнение идёт напрямую по Material. Дополнительно можно включить проверку
     * PDC-метки timespeed:cursed_ring для кастомных предметов.
     */
    private boolean hasCursedRing(Player player) {
        boolean checkMaterials = !Config.CURSED_RING_MATERIALS.isEmpty();
        boolean checkPdc = Config.CURSED_RING_PDC;
        if (!checkMaterials && !checkPdc) return false;
        for (ItemStack stack : player.getInventory().getContents()) {
            if (stack == null || stack.getType().isAir()) continue;
            if (checkMaterials && Config.CURSED_RING_MATERIALS.contains(stack.getType())) return true;
            if (checkPdc && stack.getPersistentDataContainer().has(cursedRingKey, PersistentDataType.BYTE)) {
                return true;
            }
        }
        return false;
    }

    private static void resetState(WorldSleepState st) {
        st.currentSpeed = 0f;
        st.timeAccumulator = 0f;
        st.serverSpeed = 0L;
        st.rewindStart = 0L;
        st.rewinding = false;
    }

    private static final class WorldSleepState {
        float currentSpeed;
        float timeAccumulator;
        long serverSpeed;
        long rewindStart;
        boolean rewinding;
    }
}
