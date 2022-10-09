package io.github.cardsandhuskers.tntrun.handlers;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.black_ixx.playerpoints.PlayerPointsAPI;
import org.bukkit.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.UUID;

import static io.github.cardsandhuskers.tntrun.TNTRun.*;

public class PlayerDeathHandler {
    private ArrayList<OfflinePlayer> playersList;
    private PlayerPointsAPI ppAPI;

    public PlayerDeathHandler(PlayerPointsAPI ppAPI) {
        this.ppAPI = ppAPI;
    }

    /**
     * initialize the player List
     */
    public void initList() {
        playersList = new ArrayList<>();
        for(Player p : Bukkit.getOnlinePlayers()) {
            playersList.add((OfflinePlayer) p);
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

                if(playersList.size() >= 3) {
                    p.sendMessage("You Died! You came in " + ChatColor.RED + (playersList.size() + 1) + "th" + ChatColor.RESET + " Place");
                }

                updatePoints(p);
                //round over
                break;
            }
        }
        remainingPlayers = playersList.size();
        if(playersList.size() <= 1 && gameRunning == true) {
            return true;
        } else {
            return false;
        }
    }

    public ArrayList<OfflinePlayer> getPlayersList() {
        return playersList;
    }
    public void updatePoints(Player p) {
        if(handler.getPlayerTeam(p) != null && playersList.size() > 0) {
            for (OfflinePlayer player : playersList) {
                if (handler.getPlayerTeam(player.getPlayer()) != null) {

                    player.getPlayer().sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " has fallen to their death! [" + ChatColor.GOLD + "+" + ChatColor.RED + 5 * multiplier + ChatColor.RESET + "] points!");
                    player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1);

                    Player p2 = player.getPlayer();
                    ppAPI.give(player.getUniqueId(), (int) (5 * TNTRun.multiplier));
                    handler.getPlayerTeam(p2).addTempPoints(p2, (int) (5 * multiplier));

                }
            }
            for(Player player:Bukkit.getOnlinePlayers()) {
                for(OfflinePlayer player2:playersList) {
                    if (player2.getPlayer() != null) {
                        if(player2.getPlayer().equals(player)) {
                            player.sendMessage(handler.getPlayerTeam(p).color + p.getName() + ChatColor.RESET + " has fallen to their death!");
                            //player.getPlayer().playSound(player.getPlayer().getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1,1);
                            break;
                        }
                    }
                }
            }
        }

        //when 3rd place dies, give them 25 bonus points
        if(playersList.size() == 2) {
            if(p != null && handler.getPlayerTeam(p) != null) {
                ppAPI.give(p.getUniqueId(), (int) (25 * TNTRun.multiplier));
                handler.getPlayerTeam(p).addTempPoints(p, (int) (25 * multiplier));
                p.sendMessage("You Died! You came in " + ChatColor.RED + (playersList.size() + 1) + "rd" + ChatColor.RESET + " Place [" + ChatColor.GOLD + "+" + ChatColor.RED + 25 * multiplier + ChatColor.RESET + "] points!");
            }
        }

        if(playersList.size() == 1) {
            Player p2 = playersList.get(0).getPlayer();
            //give 100 points to winner
            if(p2 != null && handler.getPlayerTeam(p2) != null) {
                ppAPI.give(p2.getUniqueId(), (int) (100 * TNTRun.multiplier));
                handler.getPlayerTeam(p2).addTempPoints(p2, (int) (100 * multiplier));
                p2.sendMessage("You Won! [" + ChatColor.GOLD + "+" + ChatColor.RED + 100 * multiplier + ChatColor.RESET + "] points!");
            }
            //player that died is 2nd place, give them 50 bonus points
            if(p != null && handler.getPlayerTeam(p) != null) {
                ppAPI.give(p.getUniqueId(), (int) (50 * TNTRun.multiplier));
                handler.getPlayerTeam(p).addTempPoints(p, (int) (50 * multiplier));
                p.sendMessage("You Died! You came in " + ChatColor.RED + (playersList.size() + 1) + "nd" + ChatColor.RESET + " Place [" + ChatColor.GOLD + "+" + ChatColor.RED + 50 * multiplier + ChatColor.RESET + "] points!");
            }
        }
    }
}
