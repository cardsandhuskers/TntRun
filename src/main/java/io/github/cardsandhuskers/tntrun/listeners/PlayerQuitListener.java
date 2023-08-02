package io.github.cardsandhuskers.tntrun.listeners;

import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

import static io.github.cardsandhuskers.tntrun.TNTRun.handler;
import static io.github.cardsandhuskers.tntrun.TNTRun.timerStatus;

public class PlayerQuitListener implements Listener {
    PlayerDeathHandler deathHandler;

    public PlayerQuitListener(PlayerDeathHandler deathHandler) {
        this.deathHandler = deathHandler;
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        if(handler.getPlayerTeam(p) == null) return;
        if(timerStatus.equals("Round Ends in")) {
            deathHandler.deathEvent(p);
        } else {
            deathHandler.removePlayer(p);
        }
    }
}
