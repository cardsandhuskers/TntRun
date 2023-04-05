package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler;
import io.github.cardsandhuskers.tntrun.handlers.TimeSinceLastMovementHandler;
import io.github.cardsandhuskers.tntrun.listeners.PlayerJoinListener;
import io.github.cardsandhuskers.tntrun.listeners.PlayerMoveListener;
import io.github.cardsandhuskers.tntrun.listeners.PlayerQuitListener;
import io.github.cardsandhuskers.tntrun.objects.Countdown;
import org.apache.commons.lang3.StringUtils;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;

import static io.github.cardsandhuskers.tntrun.TNTRun.*;
import static io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler.round;
import static org.bukkit.Bukkit.getServer;


public class StartGameCommand implements CommandExecutor {

    private TNTRun plugin;
    private RoundStartHandler roundStartHandler;
    private PlayerDeathHandler deathHandler;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;
    private final String GAME_DESCRIPTION, POINTS_DESCRIPTION;

    public StartGameCommand(TNTRun plugin) {
        this.plugin = plugin;
        GAME_DESCRIPTION = ChatColor.STRIKETHROUGH + "----------------------------------------" +
                "\n" + StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "TNT Run", 30) +
                ChatColor.BLUE + "" + ChatColor.BOLD + "\nHow To Play:" +
                ChatColor.RESET + "\nAs you run, the blocks you stand on will fall." +
                "\nThere are 4 levels, fall through all 4 and you lose." +
                "\nLast person standing or anyone who survives to the end of the timer will be declared the winner." +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
        POINTS_DESCRIPTION = "----------------------------------------" +
                ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow is the game Scored:" +
                ChatColor.RESET + "\nFor winning: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("1stPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor 2nd Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("2ndPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor 3rd Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("3rdPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor each player you outlive: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("survivalPoints") * multiplier) + ChatColor.RESET + " points" +
                ChatColor.STRIKETHROUGH + "\n----------------------------------------";
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p && p.isOp()) {
            if (plugin.getConfig().getLocation("spawnPoint") != null) {
                if(args.length > 0) {
                    if (args[0] != null) {
                        //run if at least 1 arg and it's not null
                        try {
                            TNTRun.multiplier = Double.parseDouble(args[0]);
                            startGame();

                        } catch (Exception e) {
                            p.sendMessage(ChatColor.RED + "ERROR: Argument must be an integer");
                        }
                    }
                } else {
                    //run if no arguments
                    startGame();
                }

            } else {
                p.sendMessage("Spawn Point not Set");
            }
        } else if(sender instanceof Player p) {}
        else {
            if (plugin.getConfig().getLocation("spawnPoint") != null) {
                if(args.length > 0) {
                    if (args[0] != null) {
                        //run if at least 1 arg and it's not null
                        try {
                            TNTRun.multiplier = Double.parseDouble(args[0]);
                            startGame();

                        } catch (Exception e) {
                            plugin.getLogger().warning(ChatColor.RED + "ERROR: Argument must be an integer");
                        }
                    }
                } else {
                    //run if no arguments
                    startGame();
                }



            } else {
                plugin.getLogger().warning("Spawn Point not Set");
            }



        }
        return false;
    }

    public void startGame() {
        round = 0;

        for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
            t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.NEVER);
        }
        timerStatus = "Game Starts in";
        remainingPlayers = 0;
        for(Team t:handler.getTeams()) {
            t.resetTempPoints();
            for(Player p:t.getOnlinePlayers()) {
                remainingPlayers++;
            }
        }

        for(Player p:Bukkit.getOnlinePlayers()) {
            p.teleport(plugin.getConfig().getLocation("spawnPoint"));
            Inventory inv = p.getInventory();
            inv.clear();
            if(handler.getPlayerTeam(p) == null) {
                p.setGameMode(GameMode.SPECTATOR);
            }
        }


        timeSinceLastMovementHandler = new TimeSinceLastMovementHandler(plugin);
        deathHandler = new PlayerDeathHandler();
        roundStartHandler = new RoundStartHandler(plugin, deathHandler, timeSinceLastMovementHandler);
        getServer().getPluginManager().registerEvents(new PlayerQuitListener(deathHandler), plugin);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, timeSinceLastMovementHandler), plugin);

        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                },

                //Timer End
                () -> {
                    gameStartOperations();

                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                        Bukkit.broadcastMessage(GAME_DESCRIPTION);
                    }
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) {
                        Bukkit.broadcastMessage(POINTS_DESCRIPTION);
                    }
                    if(t.getSecondsLeft() == 10) {
                        roundStartHandler.rebuildArena();
                    }

                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }

    public void gameStartOperations() {



        getServer().getPluginManager().registerEvents(new PlayerMoveListener(plugin, deathHandler, roundStartHandler, timeSinceLastMovementHandler), plugin);
        roundStartHandler.startRound();

    }

}
