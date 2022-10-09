package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetLobbyCommand implements CommandExecutor {

    private final TNTRun plugin;
    public SetLobbyCommand(TNTRun plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if(sender instanceof Player p && p.isOp()) {
            Location location = p.getLocation();

            plugin.getConfig().set("lobby", location);
            plugin.saveConfig();
            p.sendMessage("Lobby Set at " + location.toString());

        } else {
            System.out.println("Either You are not opped or you're the console. Either way, you can't do this");
        }










        return false;
    }
}
