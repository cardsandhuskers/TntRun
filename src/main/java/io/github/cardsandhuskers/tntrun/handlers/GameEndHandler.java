package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.objects.Countdown;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.tntrun.TNTRun.handler;
import static io.github.cardsandhuskers.tntrun.TNTRun.timerStatus;
import static org.bukkit.Bukkit.getServer;

public class GameEndHandler {
    private TNTRun plugin;
    public GameEndHandler(TNTRun plugin) {
        this.plugin = plugin;
    }
    public void endGame() {
        HandlerList.unregisterAll(plugin);
        for(Team t:handler.getTeams()) {
            t.resetTempPoints();
        }
        Location l = plugin.getConfig().getLocation("lobby");
        for(Player p:Bukkit.getOnlinePlayers()) {
            p.teleport(l);
        }
        for(Player p:Bukkit.getOnlinePlayers()) {
            if(p.isOp()) {
                p.performCommand("startRound");
                break;
            }
        }

        //Bukkit.broadcastMessage("Game is over");
    }


    public void gameEndTimer() {
        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("GameEndTime"),
                //Timer Start
                () -> {
                    timerStatus = "Return to Lobby in";
                    printResults();
                },

                //Timer End
                () -> {
                    endGame();
                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar  = t.getSecondsLeft();
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }
    public void printResults() {
        Countdown timer = new Countdown((JavaPlugin)plugin,
                4,
                //Timer Start
                () -> {
                    for(Team t:handler.getTeams()) {
                        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                        for(OfflinePlayer p:t.getPlayers()) {
                            if(t.getPlayerTempPoints(p) != null) {
                                tempPointsList.add(t.getPlayerTempPoints(p));
                            }
                        }
                        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                        Collections.reverse(tempPointsList);

                        for(Player p:t.getOnlinePlayers()) {
                            p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                            p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                            int number = 1;
                            for(TempPointsHolder h:tempPointsList) {
                                p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                                number++;
                            }
                            p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                        }
                    }
                },

                //Timer End
                () -> {

                },

                //Each Second
                (t) -> {
                    if(t.getSecondsLeft() == 2) {
                        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
                        for(Team team: handler.getTeams()) {
                            for(Player p:team.getOnlinePlayers()) {
                                tempPointsList.add(team.getPlayerTempPoints(p));
                            }
                        }
                        Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
                        Collections.reverse(tempPointsList);

                        int max;
                        if(tempPointsList.size() >= 5) {
                            max = 4;
                        } else {
                            max = tempPointsList.size() - 1;
                        }

                        Bukkit.broadcastMessage("\n" + ChatColor.RED + "" + ChatColor.BOLD + "Top 5 Players:");
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                        int number = 1;
                        for(int i = 0; i <= max; i++) {
                            TempPointsHolder h = tempPointsList.get(i);
                            Bukkit.broadcastMessage(number + ". " + handler.getPlayerTeam(h.getPlayer()).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " +  h.getPoints());
                            number++;
                        }
                        Bukkit.broadcastMessage(ChatColor.DARK_RED + "------------------------------");
                    }
                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }
}
