package io.github.cardsandhuskers.tntrun;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.tntrun.commands.*;
import io.github.cardsandhuskers.tntrun.handlers.PlayerDeathHandler;
import io.github.cardsandhuskers.tntrun.objects.Placeholder;
import io.github.cardsandhuskers.tntrun.objects.StatCalculator;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;


public final class TNTRun extends JavaPlugin {
    public static double multiplier = 1;
    public static boolean gameRunning = false;

    private PlayerDeathHandler deathHandler;
    //PlaceholderAPI values
    public static TeamHandler handler;
    public static int timeVar = 0;
    public static GameState gameState;
    public static int remainingPlayers = 0;
    public StatCalculator statCalculator;

    @Override
    public void onEnable() {
        handler = TeamHandler.getInstance();
        //APIs

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
        StartGameCommand startGameCommand = new StartGameCommand(this);
        getCommand("startTNTRun").setExecutor(startGameCommand);
        getCommand("reloadTNTRun").setExecutor(new ReloadConfigCommand(this));
        getCommand("cancelTNTRun").setExecutor(new CancelGameCommand(this, startGameCommand));

        statCalculator = new StatCalculator(this);
        try {
            statCalculator.calculateStats();
        } catch (Exception e) {
            StackTraceElement[] trace = e.getStackTrace();
            String str = "";
            for(StackTraceElement element:trace) str += element.toString() + "\n";
            this.getLogger().severe("ERROR Calculating Stats!\n" + str);
        }

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public enum GameState {
        GAME_STARTING,
        ROUND_STARTING,
        ROUND_ACTIVE,
        ROUND_OVER,
        GAME_OVER
    }
}
