package io.github.cardsandhuskers.tntrun.objects;

import io.github.cardsandhuskers.tntrun.TNTRun;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class StatCalculator {
    private TNTRun plugin;
    private ArrayList<PlayerStatsHolder> playerStatsHolders;
    public StatCalculator(TNTRun plugin) {
        this.plugin = plugin;
    }

    public void calculateStats() throws Exception{
        HashMap<String, PlayerStatsHolder> playerStatsMap = new HashMap<>();

        FileReader reader = null;
        try {
            reader = new FileReader(plugin.getDataFolder() + "/stats.csv");
        } catch (IOException e) {
            plugin.getLogger().warning("Stats file not found!");
            return;
        }
        String[] headers = {"Event", "Team", "Name"};

        CSVFormat.Builder builder = CSVFormat.Builder.create();
        builder.setHeader(headers);
        CSVFormat format = builder.build();

        CSVParser parser;
        try {
            parser = new CSVParser(reader, format);
        } catch (IOException e) {
            throw new Exception(e);
        }
        List<CSVRecord> recordList = parser.getRecords();

        try {
            reader.close();
        } catch (IOException e) {
            throw new Exception(e);
        }

        for(CSVRecord r:recordList) {
            String name = r.get(2);
            if(playerStatsMap.containsKey(name)) playerStatsMap.get(name).wins++;
            else playerStatsMap.put(name, new PlayerStatsHolder(name));
        }
        playerStatsHolders = new ArrayList<>(playerStatsMap.values());
        Comparator playerStatsComparator = new PlayerStatsComparator();
        playerStatsHolders.sort(playerStatsComparator);
        Collections.reverse(playerStatsHolders);

    }

    public ArrayList<PlayerStatsHolder> getPlayerStatsHolders() {
        return new ArrayList<>(playerStatsHolders);
    }


    public class PlayerStatsHolder {
        String name;
        int wins = 0;
        public PlayerStatsHolder(String name) {
            this.name = name;
            wins++;
        }
    }

    public class PlayerStatsComparator implements Comparator<PlayerStatsHolder> {
        public int compare(PlayerStatsHolder h1, PlayerStatsHolder h2) {
            int compare = Integer.compare(h1.wins, h2.wins);
            if(compare == 0) compare = h1.name.compareTo(h2.name);
            return compare;
        }
    }


}
