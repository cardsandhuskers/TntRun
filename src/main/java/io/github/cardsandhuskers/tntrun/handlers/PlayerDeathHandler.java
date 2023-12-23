package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tntrun.objects.Stats;
import io.github.cardsandhuskers.tntrun.TNTRun;
import static io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler.round;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

import static io.github.cardsandhuskers.teams.Teams.handler;
import static io.github.cardsandhuskers.tntrun.TNTRun.*;
import static io.github.cardsandhuskers.tntrun.handlers.RoundStartHandler.round;

public class PlayerDeathHandler {
    private ArrayList<OfflinePlayer> playersList;
    private HashMap<UUID, Integer> playerPositions;
    private TNTRun plugin = (TNTRun) Bukkit.getPluginManager().getPlugin("TNTRun");
    private Stats stats;

    public PlayerDeathHandler(Stats stats) {
        this.stats = stats;
    }

    /**
     * initialize the player List
     */
    public void initList() {
        playersList = new ArrayList<>();
        for(Team t: handler.getTeams()) {
            for(Player p:t.getOnlinePlayers()) {
                playersList.add((OfflinePlayer) p);
            }
        }
        playerPositions = new HashMap<>();
    }

    public void removePlayer(OfflinePlayer p) {
        if(playersList != null && playersList.contains(p)) {
            playersList.remove(p);
        }
    }

    /**
     * Called when a player 'dies' and updates the playerList
     * @param p
     * @return boolean if round is over
     */
    public boolean deathEvent(Player p) {
        for(OfflinePlayer player:playersList) {
            if(player.equals((OfflinePlayer) p)) {
                playersList.remove(player);
                p.setGameMode(GameMode.SPECTATOR);
                p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,.5F);

                int position = playersList.size() + 1;
                playerPositions.put(p.getUniqueId(), position);

                String message;
                if(position % 10 == 1) {
                    message = position + "st";
                } else if(position % 10 == 2) {
                    message = position + "nd";
                } else if(position % 10 == 3) {
                    message = position + "rd";
                } else {
                    message = position + "th";
                }

                if(playersList.size() >= 3) {
                    p.sendMessage("You Died! You came in " + ChatColor.BOLD + ChatColor.RED + message + ChatColor.RESET + " Place");
                }

                updatePoints(p);
                //round over
                break;
            }
        }

        //Round, Player, Team, Place
        String listEntry = RoundStartHandler.round + "," + p.getName() + "," + handler.getPlayerTeam(p).getTeamName() + "," + (playersList.size()+1);
        stats.addEntry(listEntry);

        remainingPlayers = playersList.size();
        if(playersList.size() <= 1 && gameRunning == true) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     * @return list of alive players
     */
    public ArrayList<OfflinePlayer> getPlayersList() {
        return playersList;
    }
    public void updatePoints(Player p) {
        if(handler.getPlayerTeam(p) != null && playersList.size() > 0) {
            double points = plugin.getConfig().getDouble("survivalPoints");
            for (OfflinePlayer player : playersList) {
                if(player.getPlayer() == null || player.getPlayer().equals(p)) continue;
                if (handler.getPlayerTeam(player.getPlayer()) != null) {
                    player.getPlayer().sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " has fallen to their death! [" + ChatColor.GOLD + "+" + ChatColor.RED + points * multiplier + ChatColor.RESET + "] points!");
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1);

                    Player p2 = player.getPlayer();
                    handler.getPlayerTeam(p2).addTempPoints(p2, points * multiplier);

                }
            }
            for(Player player:Bukkit.getOnlinePlayers()) {
                if(!playersList.contains((OfflinePlayer) player) && !player.equals(p)) {
                    player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " has fallen to their death!");
                    //player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1);
                }
            }
        }

        //when 3rd place dies, give them 25 bonus points
        if(playersList.size() == 2) {
            if(p != null && handler.getPlayerTeam(p) != null) {
                int points = plugin.getConfig().getInt("3rdPlace");
                handler.getPlayerTeam(p).addTempPoints(p, points * multiplier);
                p.sendMessage("You Died! You came in " + ChatColor.RED + (playersList.size() + 1) + "rd" + ChatColor.RESET + " Place [" + ChatColor.GOLD + "+" + ChatColor.RED + points * multiplier + ChatColor.RESET + "] points!");
            }
        }

        if(playersList.size() == 1) {
            int firstPoints = plugin.getConfig().getInt("1stPlace");
            int secondPoints = plugin.getConfig().getInt("2ndPlace");

            Player p2 = playersList.get(0).getPlayer();
            playerPositions.put(p2.getUniqueId(), 1);

            //give 100 points to winner
            if(p2 != null && handler.getPlayerTeam(p2) != null) {
                handler.getPlayerTeam(p2).addTempPoints(p2, firstPoints * multiplier);
                p2.sendMessage("You Won! [" + ChatColor.GOLD + "+" + ChatColor.RED + firstPoints * multiplier + ChatColor.RESET + "] points!");
                try {
                    saveWinner(p2);
                } catch (IOException e) {
                    plugin.getLogger().severe("ERROR SAVING WINNER!");
                }
            }
            //player that died is 2nd place, give them 50 bonus points
            if(p != null && handler.getPlayerTeam(p) != null) {
                handler.getPlayerTeam(p).addTempPoints(p, secondPoints * multiplier);
                p.sendMessage("You Died! You came in " + ChatColor.RED + (playersList.size() + 1) + "nd" + ChatColor.RESET + " Place [" + ChatColor.GOLD + "+" + ChatColor.RED + secondPoints * multiplier + ChatColor.RESET + "] points!");
            }
        }

    }

    /**
     * This saves winning player to csv, this needs to be rewritten, I want to save all positions
     * @param winner - player that won the round
     * @throws IOException
     */
    private void saveWinner(Player winner) throws IOException {
        FileWriter writer = new FileWriter("plugins/TNTRun/stats.csv", true);
        FileReader reader = new FileReader("plugins/TNTRun/stats.csv");

        String[] headers = {"Event", "Team", "Name", "Round", "Position"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser = new CSVParser(reader, format);

        if(!parser.getRecords().isEmpty()) {
            format = CSVFormat.DEFAULT;
        }

        CSVPrinter printer = new CSVPrinter(writer, format);

        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");} catch (Exception e) {eventNum = 1;}
        /*if(winner != null && handler.getPlayerTeam(winner) != null) {
            printer.printRecord(eventNum, handler.getPlayerTeam(winner).getTeamName(), winner.getDisplayName());
        }*/
        for(UUID u:playerPositions.keySet()) {
            try {
                Player p = Objects.requireNonNull(Bukkit.getPlayer(u));
                printer.printRecord(eventNum, TeamHandler.getInstance().getPlayerTeam(p).getTeamName(), p.getDisplayName(), round, playerPositions.get(u));
            } catch (NullPointerException e) {
                e.printStackTrace();
            }
        }

        writer.close();
    }

}
