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
    private Countdown gameTimer;
    private PlayerDeathHandler deathHandler;
    private TimeSinceLastMovementHandler timeSinceLastMovementHandler;

    private int numPlayers = 0;
    public RoundStartHandler(TNTRun plugin, PlayerDeathHandler deathHandler, TimeSinceLastMovementHandler timeSinceLastMovementHandler) {
        this.timeSinceLastMovementHandler = timeSinceLastMovementHandler;
        this.plugin = plugin;
        this.deathHandler = deathHandler;
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
            pregameTimer();
            deathHandler.initList();
        }
    }

    private void pregameTimer() {
        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PreRoundTime"),
                //Timer Start
                () -> {
                    teleportPlayers();
                    timerStatus = "Round Starts in";
                    remainingPlayers = 0;
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(handler.getPlayerTeam(p) != null) {
                            remainingPlayers ++;
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
        timer.scheduleTimer();
    }

    private void gameTimer() {
        gameTimer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("RoundTime"),
                //Timer Start
                () -> {
                    timerStatus = "Round Ends in";
                    for(Team t: handler.getTeams()) {
                        for(Player p:t.getOnlinePlayers()) {
                            if(p.getGameMode() != GameMode.ADVENTURE) {
                                p.teleport(plugin.getConfig().getLocation("spawnPoint"));
                            }
                            p.setGameMode(GameMode.ADVENTURE);
                            p.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, 18000, 1));
                        }
                    }
                    for(Player p:Bukkit.getOnlinePlayers()) {
                        if(handler.getPlayerTeam(p) == null) {
                            p.setGameMode(GameMode.SPECTATOR);
                        }
                        Inventory inv = p.getInventory();
                        inv.clear();
                    }

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
        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "------------------------------");
        Bukkit.broadcastMessage(StringUtils.center(ChatColor.RED + "" + ChatColor.BOLD + "Round Over!", 30));
        Bukkit.broadcastMessage(StringUtils.center(ChatColor.RED + "" + ChatColor.BOLD + "Winner(s):", 30));
        Bukkit.broadcastMessage(StringUtils.center(str2, 30));
        Bukkit.broadcastMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "------------------------------");


        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("PostRoundTime"),
                //Timer Start
                () -> {
                    if(round == 3) {
                        timerStatus = "Game Over";

                    } else {
                        timerStatus = "Preparing";
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
        timer.scheduleTimer();

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

}
