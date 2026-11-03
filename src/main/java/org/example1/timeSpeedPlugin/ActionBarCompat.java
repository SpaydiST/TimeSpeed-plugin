package org.example1.timeSpeedPlugin;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.lang.reflect.Method;

/** Optional bridge to Toolkit's shared ActionBar router. */
final class ActionBarCompat {
    private static final String TOOLKIT_PLUGIN = "Toolkit";
    private static Class<?> cachedToolkitClass;
    private static Method submitMethod;
    private static boolean warned;

    private ActionBarCompat() {}

    static void show(TimeSpeedPlugin plugin, Player player, String source, Component message,
                     int durationTicks, int priority) {
        Plugin toolkit = Bukkit.getPluginManager().getPlugin(TOOLKIT_PLUGIN);
        if (toolkit != null && toolkit.isEnabled() && submitToToolkit(plugin, toolkit, player, source,
                message, durationTicks, priority)) {
            return;
        }
        player.sendActionBar(message);
    }

    private static boolean submitToToolkit(TimeSpeedPlugin plugin, Plugin toolkit, Player player,
                                           String source, Component message, int durationTicks, int priority) {
        try {
            if (cachedToolkitClass != toolkit.getClass()) {
                cachedToolkitClass = toolkit.getClass();
                submitMethod = null;
                submitMethod = cachedToolkitClass.getMethod("submitActionBar", Player.class, String.class,
                        Component.class, int.class, int.class);
            }
            if (submitMethod == null) return false;
            submitMethod.invoke(toolkit, player, source, message, durationTicks, priority);
            return true;
        } catch (ReflectiveOperationException | RuntimeException error) {
            if (!warned) {
                plugin.getLogger().warning("Не удалось подключить ActionBar к Toolkit; сообщения TimeSpeed будут отображаться напрямую: "
                        + error.getMessage());
                warned = true;
            }
            return false;
        }
    }
}
