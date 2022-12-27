package io.github.adainish.clandorus.storage;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.util.Adapters;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClanStorage {
    public static void makeClan(Player leader) {
        File dir = Clandorus.clanStorageDir;
        dir.mkdirs();


        Clan clan = new Clan(leader);

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(clan.getClanIdentifier())));
        if (file.exists()) {
            Clandorus.log.error("There was an issue generating the Clan, Clan already exists? Ending function");
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(clan);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void makeClan(Clan clan) {
        File dir = Clandorus.clanStorageDir;
        dir.mkdirs();

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(clan.getClanIdentifier())));
        if (file.exists()) {
            Clandorus.log.error("There was an issue generating the Clan, Clan already exists? Ending function");
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(clan);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        clan.updateCache();
    }

    public static void makeTeam(Player leader, String name) {
        File dir = Clandorus.clanStorageDir;
        dir.mkdirs();


        Clan team = new Clan(leader, name);

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(team.getClanIdentifier())));
        if (file.exists()) {
            Clandorus.log.error("There was an issue generating the Player, Player already exists? Ending function");
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(team);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void safeClanRemoval(UUID clanUUID) {
        File dir = Clandorus.clanStorageDir;
        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(clanUUID)));
        file.deleteOnExit();
        if (Clandorus.clanWrapper.isClanCached(clanUUID)) {
            Clandorus.clanWrapper.clanCache.remove(clanUUID);
        }
    }

    public static void safeClanRemoval(Clan clan) {

        File dir = Clandorus.clanStorageDir;
        dir.mkdirs();

        File file = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(clan.getClanIdentifier())));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            Clandorus.log.error("Something went wrong attempting to read the Team Data");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(clan));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        clan.updateCache();
    }

    public static Clan getClan(UUID uuid) {
        File dir = Clandorus.clanStorageDir;
        dir.mkdirs();

        if (Clandorus.clanWrapper.clanCache.containsKey(uuid))
            return Clandorus.clanWrapper.clanCache.get(uuid);

        File clanFile = new File(dir, "%uuid%.json".replaceAll("%uuid%", String.valueOf(uuid)));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(clanFile));
        } catch (FileNotFoundException e) {
            Clandorus.log.error("Provided Clan did not exist in storage, could not execute action");
            return null;
        }

        return gson.fromJson(reader, Clan.class);
    }

    public static List<Clan> getAllClans()
    {
        List<Clan> clans = new ArrayList<>();
        File dir = Clandorus.clanStorageDir;
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }
                Clan clan = getClan(uuid);
                clans.add(clan);
            }
        }
        return clans;
    }

    public static List<UUID> getAllClanUUIDS()
    {
        List<UUID> uuids = new ArrayList<>();
        File dir = Clandorus.clanStorageDir;
        if (dir != null) {
            for (File f : dir.listFiles()) {
                UUID uuid;
                try {
                    uuid = UUID.fromString(f.getName().replace(".json", ""));
                } catch (IllegalArgumentException e)
                {
                    continue;
                }

                uuids.add(uuid);

            }
        }
        return uuids;
    }
}
