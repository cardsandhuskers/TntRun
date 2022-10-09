package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class SetPos2Command implements CommandExecutor {
    private final TNTRun plugin;
    public SetPos2Command(TNTRun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player p && p.isOp()) {
            Location location = p.getLocation();

            plugin.getConfig().set("pos2", location);
            plugin.saveConfig();
            p.sendMessage("Location 2 Set at: " + location.toString());

        }



        return true;
    }
}
