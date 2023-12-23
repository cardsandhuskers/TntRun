package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Collections;
import java.util.HashMap;
import java.util.UUID;

public class DoubleJumpHandler implements Runnable{

    private HashMap<UUID, Integer> remainingJumps;
    private HashMap<Player, Integer> cooldowns;
    private TNTRun plugin;
    private Integer assignedTaskId;

    public DoubleJumpHandler(TNTRun plugin) {
        this.plugin = plugin;
        remainingJumps = new HashMap<>();
        cooldowns = new HashMap<>();
    }

    /**
     * Resets the hashmaps
     */
    public void resetDoubleJumps() {
        remainingJumps.clear();
        cooldowns.clear();
    }

    /**
     * Initializes the jumps in the hashmap for the passed player
     * @param p
     */
    public void initializeJumps(Player p) {
        remainingJumps.put(p.getUniqueId(), plugin.getConfig().getInt("doubleJumps"));
    }

    /**
     * Give initial double jump feathers to player
     * @param p - player
     */
    public void giveItems(Player p) {
        ItemStack feathers = new ItemStack(Material.FEATHER, plugin.getConfig().getInt("doubleJumps"));
        ItemMeta featherMeta = feathers.getItemMeta();
        featherMeta.setLore(Collections.singletonList("Jump while in the air to use one of your double jumps!"));
        featherMeta.setDisplayName("Double Jumps Remaining");
        feathers.setItemMeta(featherMeta);

        p.getInventory().setItem(4, feathers);
    }

    /**
     * Removes a feather from the player when they double jump
     * @param p - player
     */
    public void updateItems(Player p) {
        Inventory inv = p.getInventory();
        int index = 0;
        for(ItemStack i : inv.getStorageContents()) {
            if(i == null) {

            } else if (i.getType() == Material.FEATHER) {
                ItemStack feathers = new ItemStack(Material.FEATHER, remainingJumps.getOrDefault(p.getUniqueId(), 0));
                //System.out.println(remainingJumps.getOrDefault(p.getUniqueId(), 0));
                ItemMeta featherMeta = feathers.getItemMeta();
                featherMeta.setLore(Collections.singletonList("Jump while in the air to use one of your double jumps!"));
                featherMeta.setDisplayName("Double Jumps Remaining");
                feathers.setItemMeta(featherMeta);

                inv.setItem(index, feathers);
                break;
            }
            index++;
        }
    }

    /**
     * Runs when someone double jumps
     * @param p - player
     * @return whether the jump was successful
     */
    public boolean useDoubleJump(Player p) {
        UUID u = p.getUniqueId();
        if(remainingJumps.containsKey(u)) {
            int jumps = remainingJumps.get(u);
            if(jumps > 0) {
                remainingJumps.put(u, jumps-1);
                if(cooldowns.containsKey(p)) {
                    return false;
                } else {
                    cooldowns.put(p, 0);
                    updateItems(p);
                    return true;
                }
            } else {
                return false;
            }
        } else {
            remainingJumps.put(u, plugin.getConfig().getInt("DoubleJumps"));
            return useDoubleJump(p);
        }

    }

    /**
     * puts the player in the cooldown map, used after a player double jumps
     * @param p - player
     */
    public synchronized void initCooldown(Player p) {
        cooldowns.put(p, 0);
        p.setAllowFlight(false);
    }

    /**
     * Runs every tick, counts the cooldowns for the jumps
     */
    @Override
    public synchronized void run() {
        for(Player p: cooldowns.keySet()) {
            int cooldown = cooldowns.get(p);
            if(cooldown >= 39) {
                cooldowns.remove(p);
                if(remainingJumps.get(p.getUniqueId()) > 0) p.setAllowFlight(true);
            } else {
                cooldowns.put(p, cooldown + 1);
            }

        }
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
}
