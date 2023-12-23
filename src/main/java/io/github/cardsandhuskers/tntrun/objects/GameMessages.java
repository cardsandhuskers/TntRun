package io.github.cardsandhuskers.tntrun.objects;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.teams.objects.TempPointsHolder;
import io.github.cardsandhuskers.tntrun.TNTRun;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import static io.github.cardsandhuskers.tntrun.TNTRun.multiplier;

public class GameMessages {
    private static TeamHandler handler = TeamHandler.getInstance();

    public static String getGameDescription() {
        return ChatColor.STRIKETHROUGH + "----------------------------------------" + ChatColor.RESET +
                "\n" + StringUtils.center(ChatColor.RED + "" + ChatColor.BOLD + "TNT Run", 30) +
                ChatColor.RED + "\n" + ChatColor.BOLD + "How To Play:" + ChatColor.RESET +
                "\nAs you run, the blocks you stand on will fall." +
                "\nThere are 4 levels, fall through all 4 and you lose." +
                "\nLast person standing or anyone who survives to the end of the timer will be declared the winner.\n" +
                ChatColor.STRIKETHROUGH + "----------------------------------------";
    }

    public static String getPointsDescription(TNTRun plugin) {
        return ChatColor.STRIKETHROUGH + "----------------------------------------" + ChatColor.RESET +
                ChatColor.GOLD + "" + ChatColor.BOLD + "\nHow is the game Scored:" + ChatColor.RESET +
                "\nFor winning: " + ChatColor.GOLD + (plugin.getConfig().getInt("1stPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor 2nd Place: " + ChatColor.GOLD + (plugin.getConfig().getInt("2ndPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor 3rd Place: " + ChatColor.GOLD + (int)(plugin.getConfig().getInt("3rdPlace") * multiplier) + ChatColor.RESET + " points" +
                "\nFor each player you outlive: " + ChatColor.GOLD + (plugin.getConfig().getDouble("survivalPoints") * multiplier) + ChatColor.RESET + " points\n" +
                ChatColor.STRIKETHROUGH + "----------------------------------------";

    }

    /**
     * Announces the top 5 earning players in the game
     */
    public static void announceTopPlayers() {
        ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
        for(Team team: handler.getTeams()) {
            for(Player p:team.getOnlinePlayers()) {
                tempPointsList.add(team.getPlayerTempPoints(p));
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
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

    /**
     * Announces the leaderboard for players on your team based on points earned in the game
     */
    public static void announceTeamPlayers() {
        for (Team team : handler.getTeams()) {
            ArrayList<TempPointsHolder> tempPointsList = new ArrayList<>();
            for (Player p : team.getOnlinePlayers()) {
                if (team.getPlayerTempPoints(p) != null) {
                    tempPointsList.add(team.getPlayerTempPoints(p));
                }
            }
            Collections.sort(tempPointsList, Comparator.comparing(TempPointsHolder::getPoints));
            Collections.reverse(tempPointsList);

            for (Player p : team.getOnlinePlayers()) {
                p.sendMessage(ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "Your Team Standings:");
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------");
                int number = 1;
                for (TempPointsHolder h : tempPointsList) {
                    p.sendMessage(number + ". " + handler.getPlayerTeam(p).color + h.getPlayer().getName() + ChatColor.RESET + "    Points: " + h.getPoints());
                    number++;
                }
                p.sendMessage(ChatColor.DARK_BLUE + "------------------------------\n");
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
            }
        }
    }

    /**
     * Announces the leaderboard of teams based on points earned in the game
     */
    public static void announceTeamLeaderboard() {
        ArrayList<Team> teamList = handler.getTeams();
        Collections.sort(teamList, Comparator.comparing(Team::getTempPoints));
        Collections.reverse(teamList);

        Bukkit.broadcastMessage(ChatColor.BLUE + "" + ChatColor.BOLD + "Team Leaderboard:");
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        int counter = 1;
        for(Team team:teamList) {
            Bukkit.broadcastMessage(counter + ". " + team.color + ChatColor.BOLD +  team.getTeamName() + ChatColor.RESET + " Points: " + team.getTempPoints());
            counter++;
        }
        Bukkit.broadcastMessage(ChatColor.GREEN + "------------------------------");
        for(Player p: Bukkit.getOnlinePlayers()) {
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 1);
        }
    }

    public static String announceWinner(String winner) {
        return ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "------------------------------" + ChatColor.RESET + "\n" +
                StringUtils.center(ChatColor.RED + "" + ChatColor.BOLD + "Round Over!", 30) + ChatColor.RESET + "\n" +
                StringUtils.center(ChatColor.RED + "" + ChatColor.BOLD + "Winner:", 30) + ChatColor.RESET + "\n" +
                StringUtils.center(winner, 30) + ChatColor.RESET + "\n" +
                ChatColor.DARK_AQUA + "" + ChatColor.BOLD + "------------------------------";
    }
}
