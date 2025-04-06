package io.github.cardsandhuskers.tntrun.listeners;

import io.github.cardsandhuskers.tntrun.handlers.DoubleJumpHandler;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;


public class PlayerFlyListener implements Listener {
    DoubleJumpHandler doubleJumpHandler;

    public PlayerFlyListener(DoubleJumpHandler doubleJumpHandler) {
        this.doubleJumpHandler = doubleJumpHandler;
    }

    @EventHandler
    public void onPlayerFly(PlayerToggleFlightEvent e) {
        double velHoriz = 1.3;
        double velocityY = 1.3;

        if(e.isFlying()) {
            if(e.getPlayer().getGameMode() != GameMode.ADVENTURE) return;
            System.out.println("IS FLYING");
            e.setCancelled(true);
            Player p = e.getPlayer();

            if(!doubleJumpHandler.useDoubleJump(p)) {
                return;
            }

            doubleJumpHandler.initCooldown(p);

            Location pLoc = p.getLocation();
            double yaw = pLoc.getYaw();
            if(yaw < 0) {
                yaw = 360-Math.abs(yaw);
            }
            //System.out.println("Degree yaw: " + yaw);
            yaw = Math.toRadians(yaw);
            double velocityX = velHoriz * Math.sin(yaw) * -1;
            double velocityZ = velHoriz * Math.cos(yaw);
            //p.sendMessage("JUMP: \nYaw: " + yaw + "\nSin: " + Math.sin(yaw) + "\nCos: " + Math.cos(yaw));
            //p.sendMessage("X: " + velocityX + "\nZ: " + velocityZ);

            if(pLoc.getPitch() < 0) {
                double pitch = Math.abs(pLoc.getPitch());
                pitch = Math.toRadians(pitch);
                velocityX = velocityX * Math.cos(pitch);
                velocityZ = velocityZ * Math.cos(pitch);
            }
            //p.sendMessage("X: " + velocityX + "\nZ: " + velocityZ);
            p.setVelocity(new Vector(velocityX, velocityY, velocityZ));
        }
    }
}
