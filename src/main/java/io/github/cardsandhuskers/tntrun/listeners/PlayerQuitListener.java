package io.github.cardsandhuskers.tntrun.listeners;

import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import org.bukkit.OfflinePlayer;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    PlayerDeathHandler deathHandler;

    public PlayerQuitListener(PlayerDeathHandler deathHandler) {
        this.deathHandler = deathHandler;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        OfflinePlayer p = e.getPlayer();
        deathHandler.removePlayer(p);
    }
}
