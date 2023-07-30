package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;

import static io.github.cardsandhuskers.tntrun.TNTRun.handler;

public class TimeSinceLastMovementHandler implements Runnable {

    private HashMap<OfflinePlayer, Integer> playerList;
    // Main class for bukkit scheduling
    private JavaPlugin plugin;

    // Our scheduled task's assigned id, needed for canceling
    private Integer assignedTaskId;


    // Construct a timer, you could create multiple so for example if
    // you do not want these "actions"
    public TimeSinceLastMovementHandler(JavaPlugin plugin) {
        // Initializing fields
        this.plugin = plugin;

        //build list
        playerList = new HashMap<>();
        for (Team t : handler.getTeams()) {
            for (OfflinePlayer p : t.getPlayers()) {
                playerList.put(p, 0);
            }
        }

    }

    /**
     * Actions to be completed repeatedly when the operation is active
     */
    @Override
    public void run() {
        //System.out.println(playerList.keySet());
        for (OfflinePlayer p : playerList.keySet()) {
            int ticks = playerList.get(p) + 1;
            playerList.put(p, ticks);
            if(ticks >= 5) {
                //System.out.println(p.getName() + ": " + ticks);
                deleteBlock(p.getPlayer(), ticks);
            }
        }
    }

    /**
     * resets the ticks for a player
     * @param p
     */
    public void resetTicks(Player p) {
        OfflinePlayer player = p;
        playerList.put(player, 0);
    }


    /**
     * Stop the repeating task
     */
    public void cancelOperation() {
        if (assignedTaskId != null) Bukkit.getScheduler().cancelTask(assignedTaskId);
    }


    /**
     * Schedules this instance to "run" every tick
     */
    public void startOperation() {
        // Initialize our assigned task's id, for later use so we can cancel
        this.assignedTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this, 0L, 1L);
    }

    public void addPlayer(OfflinePlayer p) {
        playerList.put(p, 0);
    }

    /**
     * Handles block deletion for a player that is standing still
     * IF ARENA IS IN NEGATIVE QUADRANTS, THIS WILL NOT WORK CORRECTLY (Arena must be in quadrant 1)
     * @param p
     * @param ticks
     */
    private void deleteBlock(Player p, int ticks) {
        Location initialLoc = p.getLocation();
        initialLoc.setY(initialLoc.getY() - 1);
        if(initialLoc.getY() % 1 == 0 && (initialLoc.getBlock().getType() == Material.AIR || ticks >= 20) && p.getGameMode() == GameMode.ADVENTURE) {
            resetTicks(p);
            Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(plugin, () -> {

                //if over air, make sure they're not  on a neighboring block
                double resultX = initialLoc.getX() % 1;
                double resultZ = initialLoc.getZ() % 1;
                //System.out.println("RESULTZ: " + resultZ);
                //hasBlocks


                //north = -Z
                //south = +Z
                //west = -X
                //east = +X

                Location l = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                Enum north = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() + 1);
                Enum south = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());
                Enum west = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                Enum east = l.getBlock().getType();



                l = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() - 1);
                Enum northEast = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() + 1);
                Enum southEast = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() - 1);
                Enum northWest = l.getBlock().getType();

                l = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() + 1);
                Enum southWest = l.getBlock().getType();




                boolean destroyed = false;
                //destroy 1 nearby block player could be standing on
                //Bukkit.broadcastMessage("X: " + resultX + "  Z: " + resultZ);
                if(!(initialLoc.getBlock().getType() == Material.AIR)) {
                    //System.out.println("BASE BLOCK CALLED");
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ());
                    destroyed = updateLocation(initialLoc);
                    //System.out.println(destroyed);
                }
                else if(resultX <= .3 && !(west == Material.AIR)) {
                    //System.out.println("NEGATIVE X CALLED");
                    //-X
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ());

                    destroyed = updateLocation(location);

                } else if(resultX >= .7 && !(east == Material.AIR)) {
                    //System.out.println("POSITIVE X CALLED");
                    //+X
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ());
                    destroyed = updateLocation(location);;

                } else if(resultZ <= .3 && !(north == Material.AIR)) {
                    //System.out.println("NEGATIVE Z CALLED");
                    //-Z
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);

                } else if(resultZ >= .7 && !(south == Material.AIR)) {
                    //System.out.println("POSITIVE Z CALLED");
                    //+Z
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX(), initialLoc.getY(), initialLoc.getZ() + 1);
                    destroyed = updateLocation(location);

                } else if(resultX >= .7 && resultZ >= .7 && !(southEast == Material.AIR)) {
                    //System.out.println("POSITIVE X POSITIVE Z CALLED");
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() + 1);
                    destroyed = updateLocation(location);

                } else if(resultX >= .7 && resultZ <= .3 && !(northEast == Material.AIR)) {
                    //System.out.println("POSITIVE X NEGATIVE Z CALLED");
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() + 1, initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);

                } else if(resultX <= .3 && resultZ >= .7 && !(southWest == Material.AIR)) {
                    //System.out.println("NEGATIVE X POSITIVE Z CALLED");
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() + 1);
                    destroyed = updateLocation(location);

                } else if(resultX <= .3 && resultZ <= .3 && !(northWest == Material.AIR)) {
                    //System.out.println("NEGATIVE X NEGATIVE Z CALLED");
                    Location location = new Location(initialLoc.getWorld(), initialLoc.getX() - 1, initialLoc.getY(), initialLoc.getZ() - 1);
                    destroyed = updateLocation(location);
                }
                if(!destroyed) {
                    //Bukkit.broadcastMessage(ChatColor.RED + "THIS MIGHT BE BAD (MoveListener)");
                }
                if(destroyed) {
                    resetTicks(p);
                    //System.out.println("TICKS RESET");
                }
            }, 5L);
        }
    }

    /**
     * Sets the 2 blocks underneath the specified location to air
     * @param l
     * @return
     */
    private boolean updateLocation(Location l) {
        if(l.getBlock().getType() == Material.SAND || l.getBlock().getType() == Material.GRAVEL) {
            l.getBlock().setType(Material.AIR);
            l.setY(l.getY() - 1);
            l.getBlock().setType(Material.AIR);
            return true;
        }
        return false;
    }
}



