package io.github.cardsandhuskers.tntrun.objects;

import io.github.cardsandhuskers.teams.handlers.TeamHandler;
import io.github.cardsandhuskers.teams.objects.Team;
import io.github.cardsandhuskers.tntrun.TNTRun;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StatCalculator {
    private TNTRun plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    public StatCalculator(TNTRun plugin) {
        this.plugin = plugin;
    }

    public void calculateStats() throws IOException{
        int initialEvent = 1;
        int eventNum;
        try {eventNum = Bukkit.getPluginManager().getPlugin("LobbyPlugin").getConfig().getInt("eventNum");}
        catch (Exception e) {eventNum = initialEvent;}

        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();
        FileReader reader;
        for(int i = initialEvent; i <= eventNum; i++) {
            try {
                reader = new FileReader(plugin.getDataFolder() + "/tntRunStats" + i + ".csv");
            } catch (IOException e) {
                plugin.getLogger().warning("Stats file not found!");
                continue;
            }
            String[] headers = {"Round", "Player", "Team", "Place"};

            CSVFormat.Builder builder = CSVFormat.Builder.create();
            builder.setHeader(headers);
            CSVFormat format = builder.build();

            CSVParser parser;
            parser = new CSVParser(reader, format);

            List<CSVRecord> recordList = parser.getRecords();
            reader.close();


            for(CSVRecord r:recordList) {
                if (r.getRecordNumber() == 1) continue;

                String name = r.get(1);
                if(playerStatsMap.containsKey(name)) {
                    playerStatsMap.get(name).addRecord(Integer.parseInt(r.get(3)));
                }
                else {
                    PlayerStatsHolder holder = new PlayerStatsHolder(name);
                    holder.addRecord(Integer.parseInt(r.get(3)));
                    playerStatsMap.put(name, holder);
                }
            }
        }

        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        playerStatsHolders.sort(new PlayerStatsComparator());

    }

    public String getPlayerFinishPosition(OfflinePlayer p) {
        String name = p.getName();
        ArrayList<PlayerStatsHolder> pph= new ArrayList<>(playerStatsHolders);

        int i = 1;
        PlayerStatsHolder playerHolder = null;
        for(PlayerStatsHolder holder: pph) {
            if(holder.name.equals(name)) {
                playerHolder = holder;
                break;
            }
            i++;
        }
        if(playerHolder == null || i <= 10) return "";

        Team team = TeamHandler.getInstance().getPlayerTeam(p.getPlayer());
        String color = "";
        if(team != null) color = team.getColor();

        return i + ". " + color + "You" + ChatColor.RESET + ": " + String.format("%.1f", playerHolder.getAveragePlacement());
    }

    public ArrayList<PlayerStatsHolder> getPlayerStatsHolders() {
        return new ArrayList<>(playerStatsHolders);
    }


    public class PlayerStatsHolder {
        String name;
        ArrayList<Integer> placements;
        public PlayerStatsHolder(String name) {
            this.name = name;
            placements = new ArrayList<>();
        }

        public void addRecord(int placement) {
            placements.add(placement);
        }
        public double getAveragePlacement() {
            double sum = 0;
            for(Integer x: placements) {
                sum += x;
            }
            sum = sum / (double)placements.size();
            return sum;
        }
    }

    public class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Double.compare(h1.getAveragePlacement(), h2.getAveragePlacement());
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }


}