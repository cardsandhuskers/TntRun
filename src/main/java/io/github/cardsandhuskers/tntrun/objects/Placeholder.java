package io.github.cardsandhuskers.tntrun.objects;

import io.github.cardsandhuskers.tntrun.TNTRun;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.util.ArrayList;

import static io.github.cardsandhuskers.tntrun.TNTRun.*;
import static io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler.round;

public class Placeholder extends PlaceholderExpansion {
    private final TNTRun plugin;

    public Placeholder(TNTRun plugin) {
        this.plugin = plugin;
    }


    @Override
    public String getIdentifier() {
        return "Tntrun";
    }
    @Override
    public String getAuthor() {
        return "cardsandhuskers";
    }
    @Override
    public String getVersion() {
        return "1.0.0";
    }
    @Override
    public boolean persist() {
        return true;
    }


    @Override
    public String onRequest(OfflinePlayer p, String s) {
        if(s.equalsIgnoreCase("timer")) {
            int mins = timeVar / 60;
            String seconds = String.format("%02d", timeVar - (mins * 60));
            return mins + ":" + seconds;
        }
        if(s.equalsIgnoreCase("timerstage")) {
            switch (gameState) {
                case GAME_STARTING: return "Game Starts in";
                case ROUND_STARTING: return "Round Starts in";
                case ROUND_ACTIVE: return "Round Ends in";
                case ROUND_OVER: return "Preparing";
                case GAME_OVER: return "Return to Lobby";
            }
        }
        if(s.equalsIgnoreCase("round")) {
            if(round == 0) {
                return round + 1 + "";
            }
            return round + "";
        }
        if(s.equalsIgnoreCase("playersLeft")) {
            return remainingPlayers + "";
        }
        String[] values = s.split("_");
        try {
            if(values[0].equalsIgnoreCase("wins")) {
                ArrayList<StatCalculator.PlayerStatsHolder> statsHolders = plugin.statCalculator.getPlayerStatsHolders();
                int index = Integer.parseInt(values[1]);
                if(index > statsHolders.size()) return "";
                StatCalculator.PlayerStatsHolder holder = statsHolders.get(Integer.parseInt(values[1]) - 1);
                String color = "";
                if (handler.getPlayerTeam(Bukkit.getPlayer(holder.name)) != null)
                    color = handler.getPlayerTeam(Bukkit.getPlayer(holder.name)).color;
                return color + holder.name + ChatColor.RESET + String.format(": %.1f", holder.getAveragePlacement());
            }


        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().warning("Error with Placeholder!\n");
        }

        return null;
    }
}
