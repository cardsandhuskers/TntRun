package io.github.cardsandhuskers.tntrun.commands;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.handlers.DoubleJumpHandler;
import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler;
import io.github.cardsandhuskers.tntrun.handlers.TimeSinceLastMovementHandler;
import io.github.cardsandhuskers.tntrun.listeners.PlayerFlyListener;
import io.github.cardsandhuskers.tntrun.listeners.PlayerJoinListener;
import io.github.cardsandhuskers.tntrun.listeners.PlayerMoveListener;
import io.github.cardsandhuskers.tntrun.listeners.PlayerQuitListener;
import io.github.cardsandhuskers.tntrun.objects.Countdown;
import io.github.cardsandhuskers.tntrun.objects.GameMessages;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import static io.github.cardsandhuskers.tntrun.TNTRun.*;
import static io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler.round;
import static org.bukkit.Bukkit.getServer;


public class StartGameCommand implements CommandExecutor {

    private final TNTRun plugin;
    private RoundStartHandler roundStartHandler;
    private PlayerDeathHandler deathHandler;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;
    private Countdown pregameTimer;

    public StartGameCommand(TNTRun plugin) {
        this.plugin = plugin;
    }

    /**
     * Command for starting the game
     * @param sender
     * @param command
     * @param label
     * @param args
     * @return
     */
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
        } else if(sender instanceof Player p) {
        } else {
            if (plugin.getConfig().getLocation("spawnPoint") != null) {
                if(args.length > 0) {
                    if (args[0] != null) {
                        //run if at least 1 arg and it's not null
                        try {
                            TNTRun.multiplier = Double.parseDouble(args[0]);
                            startGame();

                        } catch (Exception e) {
                            System.out.println(ChatColor.RED + "ERROR: Argument must be an integer");
                        }
                    }
                } else {
                    //run if no arguments
                    startGame();
                }
            } else {
                System.out.println("Spawn Point not Set");
            }
        }
        return false;
    }

    /**
     * Handles initial game starting logic, includes pregame countdown
     */
    public void startGame() {
        round = 0;
        pregameTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PregameTime"),
                //Timer Start
                () -> {
                    gameState = GameState.GAME_STARTING;
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
                            Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, ()->{p.setGameMode(GameMode.SPECTATOR);},2L);
                        }
                        p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                    }

                    timeSinceLastMovementHandler = new TimeSinceLastMovementHandler(plugin);
                    deathHandler = new PlayerDeathHandler();
                    DoubleJumpHandler doubleJumpHandler = new DoubleJumpHandler(plugin);
                    roundStartHandler = new RoundStartHandler(plugin, deathHandler, timeSinceLastMovementHandler, doubleJumpHandler);


                    getServer().getPluginManager().registerEvents(new PlayerQuitListener(deathHandler), plugin);
                    getServer().getPluginManager().registerEvents(new PlayerFlyListener(doubleJumpHandler), plugin);
                    getServer().getPluginManager().registerEvents(new PlayerJoinListener(plugin, timeSinceLastMovementHandler), plugin);

                },

                //Timer End
                () -> {
                    getServer().getPluginManager().registerEvents(new PlayerMoveListener(plugin, deathHandler, roundStartHandler, timeSinceLastMovementHandler), plugin);
                    roundStartHandler.startRound();

                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 2) Bukkit.broadcastMessage(GameMessages.getGameDescription());
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 12) Bukkit.broadcastMessage(GameMessages.getPointsDescription(plugin));

                    if(t.getSecondsLeft() == t.getTotalSeconds() - 5) {
                        roundStartHandler.rebuildArena();
                    }

                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }

    /**
     * cancels the game, calls roundHandler to cancel the timers
     */
    public void cancelGame() {
        if(pregameTimer != null) pregameTimer.cancelTimer();
        roundStartHandler.cancelGame();
    }

}
