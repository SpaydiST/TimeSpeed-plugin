package org.example1.timeSpeedPlugin;

import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class TimeSpeedPlugin extends JavaPlugin implements Listener {

    private SleepHandler sleepHandler;

    @Override
    public void onEnable() {
        Config.reload(this);
        Lang.load(this);

        for (Player player : getServer().getOnlinePlayers()) {
            ActivityTracker.onJoin(player);
        }

        sleepHandler = new SleepHandler(this);
        sleepHandler.start();

        AfkCommand afkCommand = new AfkCommand();
        getCommand("afk").setExecutor(afkCommand);
        getCommand("afk").setTabCompleter(afkCommand);

        TimeSpeedCommand timeSpeedCommand = new TimeSpeedCommand(this);
        getCommand("timespeed").setExecutor(timeSpeedCommand);
        getCommand("timespeed").setTabCompleter(timeSpeedCommand);

        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(new ActivityTracker(), this);
        getServer().getPluginManager().registerEvents(sleepHandler, this);
    }

    @Override
    public void onDisable() {
        TimeSpeedCommand.stopAll();
        if (sleepHandler != null) {
            sleepHandler.stop();
        }
        ActivityTracker.clearAll();
    }

    /**
     * Публичный доступ к обработчику сна (для других плагинов).
     */
    public SleepHandler getSleepHandler() {
        return sleepHandler;
    }

    /** Returns the active speed shown by Toolkit's clock, including manual rewinds. */
    public double getWorldTimeSpeed(World world) {
        double rewindSpeed = TimeSpeedCommand.getRewindSpeed(world);
        if (rewindSpeed > 0.0) return rewindSpeed;
        return sleepHandler == null ? 0.0 : sleepHandler.getWorldSpeed(world);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        ActivityTracker.onJoin(event.getPlayer());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        ActivityTracker.onQuit(event.getPlayer());
    }
}
