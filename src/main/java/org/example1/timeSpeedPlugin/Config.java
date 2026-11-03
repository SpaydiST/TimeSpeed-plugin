package org.example1.timeSpeedPlugin;

import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.EnumSet;
import java.util.Set;

public final class Config {

    public static boolean ENABLE_SLEEP_SYSTEM = true;
    public static double MAX_SLEEP_SPEED = 100.0;
    public static double SPEED_LERP_FACTOR = 0.15;
    public static double FAST_TIME_SPEED = 100.0;
    public static boolean ENABLE_THUNDER_SKIP = true;
    /** Разобранные один раз при загрузке материалы «проклятого кольца». */
    public static Set<Material> CURSED_RING_MATERIALS = EnumSet.noneOf(Material.class);
    /** Проверять PDC-метку timespeed:cursed_ring на предметах (для кастомных колец). */
    public static boolean CURSED_RING_PDC = false;
    public static boolean RESET_PHANTOMS_FOR_ALL = true;

    public static boolean ENABLE_AFK_SYSTEM = true;
    public static int AFK_TIMEOUT_MINUTES = 5;
    public static boolean ENABLE_MANUAL_AFK = true;

    private Config() {}

    public static void reload(JavaPlugin plugin) {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        FileConfiguration cfg = plugin.getConfig();

        ENABLE_SLEEP_SYSTEM = cfg.getBoolean("enable-sleep-system", true);
        MAX_SLEEP_SPEED = Math.max(1.0, cfg.getDouble("max-sleep-speed", 100.0));
        SPEED_LERP_FACTOR = Math.clamp(cfg.getDouble("speed-lerp-factor", 0.15), 0.01, 1.0);
        FAST_TIME_SPEED = Math.max(1.0, cfg.getDouble("fast-time-speed", 100.0));
        ENABLE_THUNDER_SKIP = cfg.getBoolean("enable-thunder-skip", true);

        CURSED_RING_MATERIALS = EnumSet.noneOf(Material.class);
        String ringCfg = cfg.getString("cursed-ring-item", "");
        if (ringCfg != null && !ringCfg.isBlank()) {
            for (String part : ringCfg.split(",")) {
                Material mat = Material.matchMaterial(part.trim());
                if (mat != null) {
                    CURSED_RING_MATERIALS.add(mat);
                } else {
                    plugin.getLogger().warning("Unknown material in cursed-ring-item: " + part.trim());
                }
            }
        }
        CURSED_RING_PDC = cfg.getBoolean("cursed-ring-pdc", false);

        RESET_PHANTOMS_FOR_ALL = cfg.getBoolean("reset-phantoms-for-all", true);

        ENABLE_AFK_SYSTEM = cfg.getBoolean("enable-afk-system", true);
        AFK_TIMEOUT_MINUTES = Math.max(1, cfg.getInt("afk-timeout-minutes", 5));
        ENABLE_MANUAL_AFK = cfg.getBoolean("enable-manual-afk", true);
    }
}
