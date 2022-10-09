package io.github.cardsandhuskers.tntrun;

import io.github.cardsandhuskers.teams.Teams;
import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.tntrun.commands.*;
import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;

import io.github.cardsandhuskers.tntrun.objects.Placeholder;
import org.black_ixx.playerpoints.PlayerPoints;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class TNTRun extends JavaPlugin {
    public static double multiplier = 1;
    public static boolean gameRunning = false;

    private PlayerDeathHandler deathHandler;
    private PlayerPointsAPI ppAPI;
    //PlaceholderAPI values
    public static TeamHandler handler;
    public static int timeVar = 0;
    public static String timerStatus = "Game Starting in";
    public static int remainingPlayers = 0;


    @Override
    public void onEnable() {
        handler = Teams.handler;
        //APIs
        if (Bukkit.getPluginManager().isPluginEnabled("PlayerPoints")) {
            this.ppAPI = PlayerPoints.getInstance().getAPI();
        } else {
            System.out.println("Could not find PlayerPointsAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        //Placeholder API validation
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            /*
             * We register the EventListener here, when PlaceholderAPI is installed.
             * Since all events are in the main class (this class), we simply use "this"
             */
            new Placeholder(this).register();

        } else {
            /*
             * We inform about the fact that PlaceholderAPI isn't installed and then
             * disable this plugin to prevent issues.
             */
            System.out.println("Could not find PlaceholderAPI! This plugin is required.");
            Bukkit.getPluginManager().disablePlugin(this);
        }


        // Plugin startup logic
        getConfig().options().copyDefaults();
        saveDefaultConfig();












        getCommand("setRunPos1").setExecutor(new SetPos1Command(this));
        getCommand("setRunPos2").setExecutor(new SetPos2Command(this));
        getCommand("saveRunArena").setExecutor(new SaveArenaCommand(this));
        getCommand("setRunSpawn").setExecutor(new SetSpawnPointCommand(this));
        getCommand("setLobby").setExecutor(new SetLobbyCommand(this));
        getCommand("startTNTRun").setExecutor(new StartGameCommand(this, ppAPI));
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
