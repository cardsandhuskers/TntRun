package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

public class CancelGameCommand implements CommandExecutor {
    TNTRun plugin;
    StartGameCommand startGameCommand;

    public CancelGameCommand(TNTRun plugin, StartGameCommand startGameCommand) {
        this.plugin = plugin;
        this.startGameCommand = startGameCommand;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if(sender instanceof Player p && p.isOp()) {
            cancelGame();
        } else if(sender instanceof Player p) {
            p.sendMessage(ChatColor.RED + "You don't have permission to do this");
        }else {
            cancelGame();
        }
        return true;
    }

    public void cancelGame() {
        HandlerList.unregisterAll(plugin);
        startGameCommand.cancelGame();
    }
}
