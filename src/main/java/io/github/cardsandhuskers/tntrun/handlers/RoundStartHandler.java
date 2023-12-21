package io.github.cardsandhuskers.tntrun.handlers;

import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.session.ClipboardHolder;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.objects.Countdown;
import io.github.cardsandhuskers.tntrun.objects.GameMessages;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import static io.github.cardsandhuskers.tntrun.TNTRun.*;


public class RoundStartHandler {
    private TNTRun plugin;
    public static int round = 0;
    private Countdown pregameTimer, gameTimer, gameOverTimer;
    private PlayerDeathHandler deathHandler;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;
    private DoubleJumpHandler doubleJumpHandler;

    private int numPlayers = 0;
    public RoundStartHandler(TNTRun plugin, PlayerDeathHandler dh, TimeSinceLastMovementHandler tslmh, DoubleJumpHandler djh) {
        timeSinceLastMovementHandler = tslmh;
        this.plugin = plugin;
        deathHandler = dh;
        doubleJumpHandler = djh;
    }

    /**
     * Initializes the round start protocol
     */
    public void startRound() {
        TNTRun.gameRunning = false;
        round++;
        numPlayers = 0;

        if(round > 3) {
            round--;
            rebuildArena();
            GameEndHandler gameEndHandler = new GameEndHandler(plugin);
            gameEndHandler.gameEndTimer();
        } else {
            preroundTimer();
            deathHandler.initList();
        }
    }

    /**
     * Runs before the round starts, countdown time where players can run around before blocks start disappearing
     */
    private void preroundTimer() {
        pregameTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PreRoundTime"),
                //Timer Start
                () -> {
                    teleportPlayers();
                    gameState = GameState.ROUND_STARTING;
                    remainingPlayers = 0;
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(handler.getPlayerTeam(p) != null) {
                            remainingPlayers ++;
                            p.getInventory().clear();
                            doubleJumpHandler.giveItems(p);
                            p.setAllowFlight(false);
                        }
                    }
                },

                //Timer End
                () -> {
                    gameRunning = true;
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        p.sendTitle(ChatColor.AQUA + "Start!", "", 2, 10, 2);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 2);
                    }
                    timeSinceLastMovementHandler.startOperation();
                    gameTimer();
                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();
                    if(t.getSecondsLeft() < 5) {
                        for(Player p:Bukkit.getOnlinePlayers()) {
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
                            p.setHealth(20);
                            p.setSaturation(20);
                            p.setFoodLevel(20);
                        }
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        pregameTimer.scheduleTimer();
    }

    /**
     * Timer that runs during the game, just a countdown until round should be forced to end
     */
    private void gameTimer() {
        gameTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("RoundTime"),
                //Timer Start
                () -> {
                    gameState = GameState.ROUND_ACTIVE;
                    for(Team t: handler.getTeams()) {
                        for(Player p:t.getOnlinePlayers()) {
                            if(p.getGameMode() != GameMode.ADVENTURE) {
                                p.teleport(plugin.getConfig().getLocation("spawnPoint"));
                            }
                            p.setGameMode(GameMode.ADVENTURE);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                            p.setAllowFlight(true);
                            doubleJumpHandler.initializeJumps(p);
                        }
                    }
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(handler.getPlayerTeam(p) == null) {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                    }
                    doubleJumpHandler.startOperation();

                },

                //Timer End
                () -> {
                    gameRunning = false;
                    reset();
                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();
                    //Bukkit.broadcastMessage("Time Left: " + t.getSecondsLeft());
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        gameTimer.scheduleTimer();
    }


    /**
     * Rebuilds the arena from the file
     * @throws IOException
     */
    public void rebuildArena() {
        BukkitWorld weWorld = new BukkitWorld(plugin.getConfig().getLocation("pos1").getWorld());

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            Clipboard clipboard;
            File file = new File(plugin.getDataFolder(), "arena.schem");
            if (!file.exists()) {
                plugin.getLogger().warning("Arena Schematic does not exist! Cannot build arena until it is saved.");
                return;
            }

            ClipboardFormat format = ClipboardFormats.findByFile(file);
            try (ClipboardReader reader = format.getReader(new FileInputStream(file))) {
                clipboard = reader.read();

                try (EditSession editSession = WorldEdit.getInstance().newEditSession(weWorld)) {
                    Operation operation = new ClipboardHolder(clipboard)
                            .createPaste(editSession)
                            .to(clipboard.getOrigin())
                            // configure here
                            .build();
                    Operations.complete(operation);

                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            Bukkit.broadcastMessage(ChatColor.GRAY + "" + ChatColor.ITALIC + "ARENA RESET COMPLETE");
        });
    }

    /**
     * Initializes the reset process to end the round and start the next one
     */
    public void reset() {
        gameTimer.cancelTimer();
        timeSinceLastMovementHandler.cancelOperation();
        doubleJumpHandler.cancelOperation();
        doubleJumpHandler.resetDoubleJumps();

        ArrayList<OfflinePlayer> players = deathHandler.getPlayersList();
        if(players.size() > 1) {
            int points;
            if(players.size() == 2) {
                points = 150;
            } else {
                points = 175;
            }
            for(OfflinePlayer p:players) {
                if(p.getPlayer() != null & handler.getPlayerTeam(p.getPlayer()) != null) {
                    handler.getPlayerTeam(p.getPlayer()).addTempPoints(p.getPlayer(), (points/players.size()) * multiplier);
                    p.getPlayer().sendMessage("You Won! [" + ChatColor.GOLD + "+" + ChatColor.RED + (points/players.size()) * multiplier + ChatColor.RESET + "] points!");
                }
            }
        }
        String str;
        String str2 = "";
        if(players.size() > 1) {
            str = "Winners: ";
            for(OfflinePlayer p:players) {
                if(p.getPlayer() != null && handler.getPlayerTeam(p.getPlayer()) != null) {
                    str += handler.getPlayerTeam(p.getPlayer()).color + p.getPlayer().getName() + " ";
                    str2 += handler.getPlayerTeam(p.getPlayer()).color + p.getPlayer().getName() + " ";
                }
            }

        } else if(players.size() > 0){
            str = "Winner: ";
            if(players.get(0).getPlayer() != null && handler.getPlayerTeam(players.get(0).getPlayer()) != null) {
                str += handler.getPlayerTeam(players.get(0).getPlayer()).color + players.get(0).getName();
                str2 += handler.getPlayerTeam(players.get(0).getPlayer()).color + players.get(0).getName();
            }
        } else {
            str = "";
        }

        for(Player p:Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1, .5F);
            p.sendTitle(ChatColor.GREEN + "Round Over!", str, 5, 30, 5);
            p.setGameMode(GameMode.SPECTATOR);
        }
        GameMessages.announceWinner(str2);


        gameOverTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PostRoundTime"),
                //Timer Start
                () -> {
                    if(round == 3) {
                        gameState = GameState.GAME_OVER;

                    } else {
                        gameState = GameState.ROUND_OVER;
                    }
                    rebuildArena();

                    gameRunning = false;
                },

                //Timer End
                () -> {
                    startRound();

                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar = t.getSecondsLeft();
                }
        );
        gameOverTimer.scheduleTimer();

    }

    /**
     * Teleports all players to the arena spawn point
     */
    private void teleportPlayers() {
        for (Team t: handler.getTeams()) {
            for(Player p :t.getOnlinePlayers()) {
                p.teleport(plugin.getConfig().getLocation("spawnPoint"));
                p.setGameMode(GameMode.ADVENTURE);
                p.setSwimming(false);
                Inventory inv = p.getInventory();
                inv.clear();
                numPlayers++;
                p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
            }
        }
    }

    public void cancelGame() {
        if(pregameTimer != null) {
            pregameTimer.cancelTimer();
        }
        if(gameTimer != null) {
            gameTimer.cancelTimer();
        }
        if(gameOverTimer != null) {
            gameOverTimer.cancelTimer();
        }
    }

}
