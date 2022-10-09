package io.github.cardsandhuskers.tntrun.objects;

import io.github.cardsandhuskers.tntrun.TNTRun;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;

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
            return timerStatus;
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
        return null;
    }
}
