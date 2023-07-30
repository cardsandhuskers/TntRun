package io.github.cardsandhuskers.tntrun;

import org.bukkit.Location;

import java.io.Serializable;
import java.util.HashMap;

/**
 * IDK wtf this even is or why it's here, I don't even think I wrote this, gonna keep it around for now, but it does absolutely nothing
 */
public class MapData implements Serializable {
    private static transient final long serialVersionUID = -1681012206529286330L;
    public final HashMap<Location, String> blockLocations;


    public MapData(HashMap<Location, String> blockLocations) {
        this.blockLocations = blockLocations;
    }
    public MapData(MapData loadedData) {
        this.blockLocations = loadedData.blockLocations;

    }
    /*
    public boolean saveData(String filePath) {
        try {
            BukkitObjectOutputStream out = new BukkitObjectOutputStream(new GZIPOutputStream(new FileOutputStream(filePath)));
            out.writeObject(this);
            out.close();
            return true;
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
    }
    public static MapData loadData(String filePath) {
        try {
            BukkitObjectInputStream in = new BukkitObjectInputStream(new GZIPInputStream(new FileInputStream(filePath)));
            MapData data = (MapData) in.readObject();
            in.close();
            return data;
        } catch (ClassNotFoundException | IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return null;
        }
    }
    public static void getBlocks(TNTRun plugin) {

        HashMap<Location, String> blockSnapShot = new HashMap<Location, String>();

        int x, y, z;

        Location blockLocation;
        String higherx;
        String lowerx;
        String highery;
        String lowery;
        String higherz;
        String lowerz;
        if(getCoordinate("pos1", 'x', plugin) > getCoordinate("pos2", 'x', plugin)) {
            higherx = "pos1";
            lowerx = "pos2";
        } else {
            higherx = "pos2";
            lowerx = "pos1";
        }
        if(getCoordinate("pos1", 'y', plugin) > getCoordinate("pos2", 'y', plugin)) {
            highery = "pos1";
            lowery = "pos2";
        } else {
            highery = "pos2";
            lowery = "pos1";
        }
        if(getCoordinate("pos1", 'z', plugin) > getCoordinate("pos2", 'z', plugin)) {
            higherz = "pos1";
            lowerz = "pos2";
        } else {
            higherz = "pos2";
            lowerz = "pos1";
        }
        Location init = new Location(plugin.getConfig().getLocation("pos1").getWorld(), getCoordinate(lowerx, 'x', plugin), getCoordinate(lowery, 'y', plugin), getCoordinate(lowerz, 'z', plugin));


        for(x = getCoordinate(lowerx, 'x', plugin); x <= getCoordinate(higherx, 'x', plugin); x++) {
            for(y = getCoordinate(lowery, 'y', plugin); y <= getCoordinate(highery, 'y', plugin); y++) {
                for(z = getCoordinate(lowerz, 'z', plugin); z <= getCoordinate(higherz, 'z', plugin); z++) {

                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() - x,
                            init.getY() - y,
                            init.getZ() - z), blockLocation.getBlock().getBlockData().getAsString());
                    //System.out.println(blockLocation.toString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() + x,
                            init.getY() - y,
                            init.getZ() - z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() - x,
                            init.getY() + y,
                            init.getZ() - z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() - x,
                            init.getY() - y,
                            init.getZ() + z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() + x,
                            init.getY() + y,
                            init.getZ() + z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() - x,
                            init.getY() + y,
                            init.getZ() + z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() + x,
                            init.getY() - y,
                            init.getZ() + z), blockLocation.getBlock().getBlockData().getAsString());
                    blockSnapShot.put(blockLocation = new Location(init.getWorld(),
                            init.getX() + x,
                            init.getY() + y,
                            init.getZ() - z), blockLocation.getBlock().getBlockData().getAsString());
                }
            }
        }
        // Finally we save the retrieved data to a file

        // You will most likely want to change the file location to your some other directory,
        // like your plugin's data directory, instead of the Tutorial's.
        new MapData(blockSnapShot).saveData("Arena.data");
        Bukkit.getServer().getLogger().log(Level.INFO, "Data Saved");

    }
    public static int getCoordinate(String pos, char axis, TNTRun plugin) {
        Location l = plugin.getConfig().getLocation(pos);
        if(axis == 'x') {
            return l.getBlockX();
        }
        if(axis == 'y') {
            l.getY();
            return l.getBlockY();
        }
        if(axis == 'z') {
            l.getZ();
            return l.getBlockZ();
        }
        else {
            return 0;
        }
    }
    public static void resetBlocks() {
        MapData data = new MapData(MapData.loadData("Arena.data"));
        // For each player that is current online send them a message
        // Change all of the blocks around the spawn to what we have saved in our file.
        data.blockLocations.keySet().forEach(location -> location.getBlock().setBlockData(Bukkit.getServer().createBlockData(data.blockLocations.get(location))));
        Bukkit.getServer().getLogger().log(Level.INFO, "Data loaded");
    }

     */

}
