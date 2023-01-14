package io.github.cardsandhuskers.tntrun.listeners;

import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler;
import io.github.cardsandhuskers.tntrun.handlers.TimeSinceLastMovementHandler;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

import static io.github.cardsandhuskers.tntrun.TNTRun.gameRunning;

public class PlayerMoveListener implements Listener {
    private TNTRun plugin;
    private PlayerDeathHandler deathHandler;
    private RoundStartHandler roundStartHandler;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;

    private int tempX = 0;
    private int tempZ = 0;

    public PlayerMoveListener(TNTRun plugin, PlayerDeathHandler deathHandler, RoundStartHandler roundStartHandler, TimeSinceLastMovementHandler timeSinceLastMovementHandler) {
        this.timeSinceLastMovementHandler = timeSinceLastMovementHandler;
        this.roundStartHandler = roundStartHandler;
        this.plugin = plugin;
        this.deathHandler = deathHandler;
    }

    public boolean updateLocation(Location l) {
        if(l.getBlock().getType() != Material.AIR) {
            l.getBlock().setType(Material.AIR);
            l.setY(l.getY() - 1);
            l.getBlock().setType(Material.AIR);
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        Player p = e.getPlayer();
        if(gameRunning && p.getGameMode() == GameMode.ADVENTURE) {

            //System.out.println(p + " moved");
            Location initialLoc = p.getLocation();
            Date date = new Date();
            AtomicLong time = new AtomicLong(date.getTime());

            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                Location finalLoc = p.getLocation();

                double deltaX = finalLoc.getX() - initialLoc.getX();
                double deltaY = finalLoc.getY() - initialLoc.getY();
                double deltaZ = finalLoc.getZ() - initialLoc.getZ();

                //Bukkit.broadcastMessage("X: " + deltaX + "Z: " + deltaZ);
                double trueVelocity = Math.sqrt(Math.pow(deltaX, 2) + Math.pow(deltaZ, 2));
                //Bukkit.broadcastMessage("XY: " + Math.abs(trueVelocity));


                //not jumping, just walking/running
                if (deltaY == 0 && Math.abs(trueVelocity) > 0) {
                    timeSinceLastMovementHandler.resetTicks(p);
                    initialLoc.setY(initialLoc.getY() - 1);
                    if(!(initialLoc.getBlock().getX() == tempX && initialLoc.getBlock().getZ() == tempZ)) {

                        tempX = initialLoc.getBlock().getX();
                        tempZ = initialLoc.getBlock().getZ();

                        if(initialLoc.getBlock().getType().equals(Material.SAND) || initialLoc.getBlock().getType().equals(Material.GRAVEL)) {
                            //Bukkit.broadcastMessage("NOT AIR");
                            //Bukkit.broadcastMessage("X: " + initialLoc.getBlock().getX() + "Z: " + initialLoc.getBlock().getZ());
                            //DELAY
                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ());
                                updateLocation(loc);
                            }, 4L);
                        } else if (initialLoc.getBlock().getType() == Material.AIR){
                            //Bukkit.broadcastMessage("AIR");
                            //Bukkit.broadcastMessage("X: " + initialLoc.getBlock().getX() + "Z: " + initialLoc.getBlock().getZ());
                            //if they're running on air
                            double resultX = initialLoc.getX() % 1;
                            double resultZ = initialLoc.getZ() % 1;
                            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                                if(deltaX > deltaZ) {
                                    if(resultX >= .7) {
                                        Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                                        updateLocation(loc);
                                    } else if(resultX <= .3) {
                                        Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());
                                        updateLocation(loc);
                                    }
                                } else {
                                    if(resultZ >= .7) {
                                        Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() + 1);
                                        updateLocation(loc);
                                    } else if(resultZ <= .3) {
                                        Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                                        updateLocation(loc);
                                    }
                                }
                            }, 4L);
                        }
                    }
                }

                //sprint jumping
                //if Y is moving and if they've landed on a block

                else if (deltaY != 0 && initialLoc.getY() % 1 == 0 && Math.abs(trueVelocity) > .48) {
                    //Bukkit.broadcastMessage("SPRINT JUMPING");
                    //System.out.println("X: " + deltaX + "Z: " + deltaZ);
                    timeSinceLastMovementHandler.resetTicks(p);

                    initialLoc.setY(initialLoc.getY() - 1);
                    jump(initialLoc);
                    Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                        if(Math.abs(Math.abs(deltaX) - Math.abs(deltaZ)) <= .3) {
                            //DIAGONAL
                            if(deltaX > 0 && deltaZ > 0) {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() + 1);
                                updateLocation(loc);
                            } else if(deltaX > 0 && deltaZ < 0) {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() - 1);
                                updateLocation(loc);
                            } else if(deltaX < 0 && deltaZ > 0) {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() + 1);
                                updateLocation(loc);
                            } else {//both negative
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() - 1);
                                updateLocation(loc);
                            }
                        } else if(Math.abs(deltaZ) > Math.abs(deltaX)) {
                            //mostly Z
                            if(deltaZ > 0) {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() + 1);
                                updateLocation(loc);
                            } else {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                                updateLocation(loc);
                            }
                        } else {
                            //mostly X
                            if(deltaX > 0) {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                                updateLocation(loc);
                            } else {
                                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());
                                updateLocation(loc);
                            }
                        }
                    }, 3L);
                }

                //regular jumping
                else if (deltaY != 0 && initialLoc.getY() % 1 == 0 && trueVelocity > 0 && Math.abs(trueVelocity) <= .48) {
                    timeSinceLastMovementHandler.resetTicks(p);
                    //Bukkit.broadcastMessage("REGULAR JUMPING");
                    initialLoc.setY(initialLoc.getY() - 1);
                    jump(initialLoc);

                }
            }, 2L);
            if(p.getLocation().getY() <= 1) {
                boolean ended = deathHandler.deathEvent(p);
                if(ended) {
                    roundStartHandler.reset();
                }
            }
        }
    }
    public void jump(Location initialLoc) {
        if(initialLoc.getBlock().getType().equals(Material.SAND) || initialLoc.getBlock().getType().equals(Material.GRAVEL)) {
            //DELAY
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                Location loc = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ());
                loc.getBlock().setType(Material.AIR);
                loc.setY(loc.getY() - 1);
                loc.getBlock().setType(Material.AIR);
                //see if they're closer to the x or z edge
                //+X
                double resultX = initialLoc.getX() % 1;
                double resultZ = initialLoc.getZ() % 1;
                double distX;
                double distZ;

                //get distance from X and Z
                if(resultX >= .5) {
                    distX = 1-resultX;
                } else {
                    distX = resultX;
                }
                if(resultZ >= .5) {
                    distZ = 1-resultZ;
                } else {
                    distZ = resultZ;
                }


                //if closer to X edge
                if(distX < distZ) {
                    if(resultX >= .9) {
                        //if + edge
                        Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                        updateLocation(location);

                    } else if(resultX <= .1){
                        //if - edge
                        Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());
                        updateLocation(location);
                    }
                } else {
                    //if closer to Z edge
                    if(resultZ >= .9) {
                        //if + edge
                        Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() + 1);
                        updateLocation(location);

                    } else if(resultZ <= .1){
                        //if - edge
                        Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                        updateLocation(location);
                    }
                }

            }, 3L);
        } else if(initialLoc.getBlock().getType().equals(Material.AIR)){
            //DELAY
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                //if over air, make sure they're not  on a neighboring block
                double resultX = initialLoc.getX() % 1;
                double resultZ = initialLoc.getZ() % 1;


                boolean destroyed = false;
                //destroy 1 nearby block player could be standing on
                if(resultX <= .3 && !destroyed) {
                    //-X
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());

                    destroyed = updateLocation(location);

                } if(resultX >= .7 && !destroyed) {
                    //+X
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                    destroyed = updateLocation(location);;

                } if(resultZ <= .3 && !destroyed) {
                    //-Z
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);

                } if(resultZ >= .7 && !destroyed) {
                    //+Z
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);

                } if(resultX >= .7 && resultZ >= .7 && !destroyed) {
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() + 1);
                    destroyed = updateLocation(location);

                } if(resultX >= .7 && resultZ <= .3 && !destroyed) {
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);

                } if(resultX <= .3 && resultZ >= .7 && !destroyed) {
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() + 1);
                    destroyed = updateLocation(location);

                } if(resultX <= .3 && resultZ <= .3 && !destroyed) {
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);
                }
                //if(!destroyed) {
                    //Bukkit.broadcastMessage(ChatColor.RED + "THIS IS BAD (MoveListener)");
                //}
            }, 3L);
        }
    }

}
