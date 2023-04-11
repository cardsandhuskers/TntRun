package io.github.cardsandhuskers.tntrun.listeners;

import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.handlers.TimeSinceLastMovementHandler;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {
    TNTRun plugin;
    TimeSinceLastMovementHandler timeSinceLastMovementHandler;

    public PlayerJoinListener(TNTRun plugin, TimeSinceLastMovementHandler timeSinceLastMovementHandler) {
        this.plugin = plugin;
        this.timeSinceLastMovementHandler = timeSinceLastMovementHandler;
    }

    @EventHandler
    public void playerJoinEvent(PlayerJoinEvent e) {
        //System.out.println("JOINED");
        Player p = e.getPlayer();
        p.teleport(plugin.getConfig().getLocation("spawnPoint"));
        timeSinceLastMovementHandler.addPlayer(p);

        Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
            p.setGameMode(GameMode.SPECTATOR);
        },10L);
    }
}
