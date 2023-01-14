package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SaveArenaCommand implements CommandExecutor {
    private final TNTRun plugin;

    public SaveArenaCommand(TNTRun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p && p.isOp()) {

            //figure out which corner has the higher value for X, Y, and Z axis
            String higherx;
            String lowerx;
            String highery;
            String lowery;
            String higherz;
            String lowerz;
            if (getCoordinate("pos1", 'x') > getCoordinate("pos2", 'x')) {
                higherx = "pos1";
                lowerx = "pos2";
            } else {
                higherx = "pos2";
                lowerx = "pos1";
            }
            if (getCoordinate("pos1", 'y') > getCoordinate("pos2", 'y')) {
                highery = "pos1";
                lowery = "pos2";
            } else {
                highery = "pos2";
                lowery = "pos1";
            }
            if (getCoordinate("pos1", 'z') > getCoordinate("pos2", 'z')) {
                higherz = "pos1";
                lowerz = "pos2";
            } else {
                higherz = "pos2";
                lowerz = "pos1";
            }

            //save tnt and sand locations
            LinkedHashMap<Location, Material> blockMap = new LinkedHashMap<>();
            //3 loops to iterate on 3 axes
            for (int x = getCoordinate(lowerx, 'x'); x <= getCoordinate(higherx, 'x'); x++) {
                for (int z = getCoordinate(lowerz, 'z'); z <= getCoordinate(higherz, 'z'); z++) {
                    for (int y = getCoordinate(lowery, 'y'); y <= getCoordinate(highery, 'y'); y++) {
                        //make location
                        Location loc = new Location(plugin.getConfig().getLocation("pos1").getWorld(), x, y, z);
                        if(loc.getBlock().getType() == Material.TNT || loc.getBlock().getType() == Material.SAND ||loc.getBlock().getType() == Material.GRAVEL) {
                            blockMap.put(loc, loc.getBlock().getType());
                            //System.out.println(loc);
                        }
                    }
                }
            }
            for(Location l: blockMap.keySet()) {
                //System.out.println(l);
            }

//SAVE DATA
            File arenaFile = new File(Bukkit.getServer().getPluginManager().getPlugin("TNTRun").getDataFolder(), "arena.yml");
            if (!arenaFile.exists()) {
                try {
                    arenaFile.createNewFile();
                } catch (IOException e) {
                    System.out.println("ERROR CREATING FILE");
                }
            }
            FileConfiguration arenaFileConfig = YamlConfiguration.loadConfiguration(arenaFile);


            //System.out.println(blockMap);
            arenaFileConfig.set("blocks", new HashMap<>());
            int index = 0;
            for (Map.Entry<Location, Material> e : blockMap.entrySet()) {

                String s = "blocks." + index + ".";
                Location l = e.getKey();
                arenaFileConfig.set("world", l.getWorld().getName());

                arenaFileConfig.set(s + "Block", e.getValue().toString());
                arenaFileConfig.set(s + "x", l.getX());
                arenaFileConfig.set(s + "y", l.getY());
                arenaFileConfig.set(s + "z", l.getZ());
                index++;
            }

            //System.out.println(index);
            try {
                arenaFileConfig.save(arenaFile);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        return true;
    }
    public int getCoordinate(String pos, char axis) {
        Location l = plugin.getConfig().getLocation(pos);
        switch(axis) {
            case 'x': return l.getBlockX();
            case 'y': return l.getBlockY();
            case 'z': return l.getBlockZ();
            default: return 0;
        }
    }

}
