package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.tntrun.TNTRun;
import io.github.cardsandhuskers.tntrun.objects.Countdown;
import io.github.cardsandhuskers.tntrun.objects.GameMessages;
import io.github.cardsandhuskers.tntrun.objects.Stats;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import static io.github.cardsandhuskers.tntrun.TNTRun.timerStatus;

public class GameEndHandler {
    private TNTRun plugin;
    private Stats stats;
    public GameEndHandler(TNTRun plugin, Stats stats) {
        this.plugin = plugin;
        this.stats = stats;
    }
    public void endGame() {
        HandlerList.unregisterAll(plugin);
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
        try {
            plugin.statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            plugin.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

        //Bukkit.broadcastMessage("Game is over");
    }


    public void gameEndTimer() {
        Countdown timer = new Countdown((JavaPlugin)plugin,
                plugin.getConfig().getInt("GameEndTime"),
                //Timer Start
                () -> {
                    timerStatus = "Return to Lobby in";
                    stats.writeToFile(plugin.getDataFolder().toPath().toString(), "tntRunStats");
                },

                //Timer End
                () -> {
                    endGame();
                    
                    //for(org.bukkit.scoreboard.Team t:Bukkit.getScoreboardManager().getMainScoreboard().getTeams()) {
                    //    t.setOption(org.bukkit.scoreboard.Team.Option.COLLISION_RULE, org.bukkit.scoreboard.Team.OptionStatus.ALWAYS);
                    //}
                },

                //Each Second
                (t) -> {
                    TNTRun.timeVar  = t.getSecondsLeft();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 1) GameMessages.announceTopPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 6) GameMessages.announceTeamPlayers();
                    if(t.getSecondsLeft() == t.getTotalSeconds() - 11) GameMessages.announceTeamLeaderboard();

                }
        );

        // Start scheduling, don't use the "run" method unless you want to skip a second
        timer.scheduleTimer();
    }
}
