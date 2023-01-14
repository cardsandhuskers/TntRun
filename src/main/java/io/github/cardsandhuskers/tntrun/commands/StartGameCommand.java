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
import org.black_ixx.playerpoints.PlayerPointsAPI;

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

    private final TNTRun plugin;
    private RoundStartHandler roundStartHandler;
    private PlayerDeathHandler deathHandler;
    private PlayerPointsAPI ppAPI;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;

    public StartGameCommand(TNTRun plugin, PlayerPointsAPI ppAPI) {
        this.plugin = plugin;
        this.ppAPI = ppAPI;
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
        }
        return false;
    }

    public void startGame() {
        round = 0;
        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
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
                    deathHandler = new PlayerDeathHandler(ppAPI);
                    roundStartHandler = new RoundStartHandler(plugin, deathHandler, timeSinceLastMovementHandler, ppAPI);
                    getServer().getPluginManager().registerEvents(new PlayerQuitListener(deathHandler), plugin);
                    getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, timeSinceLastMovementHandler), plugin);

                },

                //Timer End
                () -> {
                    gameStartOperations();

                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(StringUtils.center(ChatColor.LIGHT_PURPLE + "" + ChatColor.BOLD + "TNT Run", 30));
                        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "How To Play:");
                        Bukkit.broadcastMessage("As you run, the blocks you stand on will fall." +
                                "\nThere are 4 levels, fall through all 4 and you lose." +
                                "\nLast person standing or anyone who survives to the end of the timer will be declared the winner.");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) {
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                        Bukkit.broadcastMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "How is the game Scored:");
                        Bukkit.broadcastMessage("For winning: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("1stPlace") * multiplier) + ChatColor.RESET + " points" +
                                                "\nFor 2nd Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("2ndPlace") * multiplier) + ChatColor.RESET + " points" +
                                                "\nFor 3rd Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("3rdPlace") * multiplier) + ChatColor.RESET + " points" +
                                                "\nFor each player you outlive: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("survivalPoints") * multiplier) + ChatColor.RESET + " points");
                        Bukkit.broadcastMessage(ChatColor.STRIKETHROUGH + "----------------------------------------");
                    }
                    if(t.getSecondsLeft() == 10) {
                        try {
                            roundStartHandler.rebuildArena();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
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
