package io.github.adainish.clandorus.registry;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.stream.JsonReader;
import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import info.pixelmon.repack.org.spongepowered.CommentedConfigurationNode;
import info.pixelmon.repack.org.spongepowered.ConfigurateException;
import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.ClanGymConfig;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.util.Adapters;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ClanGymRegistry {

    public HashMap<String, ClanGym> clanGymCache = new HashMap<>();


    public void saveAll()
    {
        for (ClanGym cl:clanGymCache.values()) {
            cl.save();
        }
    }

    public void load()
    {
        if (!clanGymCache.isEmpty())
        {
            saveAll();
        }
        loadFromStorage();
        loadFromConfig();
    }

    public void loadFromConfig()
    {
        Clandorus.log.warn("Loading (new) Clan Gyms from config, if no new clan gyms have been created this won't take long!");
        CommentedConfigurationNode node = ClanGymConfig.getConfig().get().node("Gyms");
        Map <Object, CommentedConfigurationNode> nodeMap = node.childrenMap();
        for (Object obj : nodeMap.keySet()) {
            if (obj == null) {
                Clandorus.log.error("OBJ Null while generating Clan Gym");
                continue;
            }
            String identifier = obj.toString();

            if (clanGymCache.containsKey(identifier))
                continue;

            if (fileInStorage(identifier))
                continue;

            ClanGym gym = new ClanGym(identifier);
            makeClanGym(gym);
            clanGymCache.put(identifier, gym);

        }
    }

    public boolean fileInStorage(String id)
    {
        File storage = Clandorus.clanGymStorageDir;
        ArrayList<File> fileList = Arrays.stream(storage.listFiles()).filter(Objects::nonNull).collect(Collectors.toCollection(ArrayList::new));

        return fileList.stream().anyMatch(f -> f.getName().replace(".json", "").equalsIgnoreCase(id));
    }

    public void makeClanGym(ClanGym clanGym) {
        File dir = Clandorus.clanGymStorageDir;
        dir.mkdirs();


        File file = new File(dir, "%id%.json".replaceAll("%id%", clanGym.getIdentifier()));
        if (file.exists()) {
            return;
        }

        Gson gson = Adapters.PRETTY_MAIN_GSON;
        String json = gson.toJson(clanGym);

        try {
            file.createNewFile();
            FileWriter writer = new FileWriter(file);
            writer.write(json);
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void loadFromStorage()
    {
        Clandorus.log.warn("Loading previously made clan gyms from the storage");
        if (Clandorus.clanGymStorageDir == null) {
            Clandorus.log.warn("Clan Gym storage was null!");
            return;
        }
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        for (File f: Clandorus.clanGymStorageDir.listFiles()) {
            if (f == null)
                continue;

            JsonReader reader = null;
            try {
                reader = new JsonReader(new FileReader(f));
                ClanGym t = gson.fromJson(reader, ClanGym.class);
                if (t != null) {
                    t.syncConfig();
                    clanGymCache.put(t.getIdentifier(), t);
                }
            } catch (FileNotFoundException | JsonSyntaxException e) {
                Clandorus.log.error(e.getMessage());
                Clandorus.log.error(e.getStackTrace());
            }
        }
    }

    public void saveAndUnload(ClanGym gym)
    {
        saveAll(gym);
        clanGymCache.remove(gym.getIdentifier());
    }

    public void saveAndUnloadAll()
    {
        for (ClanGym g:clanGymCache.values()) {
            saveAndUnload(g);
        }
    }

    public void saveGyms()
    {
        for (ClanGym g:clanGymCache.values()) {
            g.save();
        }
    }

    public List<String> alphabet()
    {
        return new ArrayList<>(Arrays.asList("a", "b", "c", "d", "e", "f", "g", "h", "i", "j", "k", "l", "m", "n", "o", "p", "q", "r", "s", "t", "u", "v", "w", "x", "y", "z"));
    }


    public String randomIDGenerator()
    {
        StringBuilder stringBuilder = new StringBuilder("AutoID");
        for (int i = 0; i < 10; i++) {
            stringBuilder.append(RandomHelper.getRandomElementFromCollection(alphabet()));
        }
        return stringBuilder.toString();
    }

    public void createAndSaveToConfig(ClanGym gym)
    {
        if (clanGymCache.containsKey(gym.getIdentifier()))
        {
            Clandorus.log.error("The provided id for this gym already existed! A new ID is being auto generated!");
            String newID = randomIDGenerator();
            gym.setIdentifier(newID);
        }
        clanGymCache.put(gym.getIdentifier(), gym);
        saveAll(gym);
    }

    public void saveEntryToConfig(ClanGym gym)
    {
        Clandorus.log.warn("Saving Clan Gym info to gym config");
        ClanGymConfig clanGymConfig = ClanGymConfig.getConfig();
        CommentedConfigurationNode configurationNode = clanGymConfig.get();
        try {
            configurationNode.node("Gyms", gym.getIdentifier(), "Location", "WorldID").set(gym.getLocation().getWorldID());
            configurationNode.node("Gyms", gym.getIdentifier(), "Location", "X").set(gym.getLocation().getPosX());
            configurationNode.node("Gyms", gym.getIdentifier(), "Location", "Y").set(gym.getLocation().getPosY());
            configurationNode.node("Gyms", gym.getIdentifier(), "Location", "Z").set(gym.getLocation().getPosZ());

            configurationNode.node("Gyms", gym.getIdentifier(), "HoldRequirements", "BannedPokemonSpecs").set(gym.getHoldRequirements().bannedPokemonSpecs);
            configurationNode.node("Gyms", gym.getIdentifier(), "HoldRequirements", "BannedAbilities").set(gym.getHoldRequirements().bannedAbilities);
            configurationNode.node("Gyms", gym.getIdentifier(), "HoldRequirements", "BannedAttacks").set(gym.getHoldRequirements().bannedMoves);
            configurationNode.node("Gyms", gym.getIdentifier(), "HoldRequirements", "MinIVS").set(gym.getHoldRequirements().min31Ivs);

            configurationNode.node("Gyms", gym.getIdentifier(), "WinAction", "RewardIDs").set(gym.getWinAction().rewardIDs);
            configurationNode.node("Gyms", gym.getIdentifier(), "WinAction", "TakePokemon").set(gym.getWinAction().takePokemon);
            configurationNode.node("Gyms", gym.getIdentifier(), "WinAction", "PokemonSpecs").set(gym.getWinAction().pokemonSpecList);
            configurationNode.node("Gyms", gym.getIdentifier(), "WinAction", "Money").set(gym.getWinAction().money);
            List<String> battleRuleData = new ArrayList <>();
            for (BattleClause bc:gym.getBattleClauses()) {
                battleRuleData.add(bc.getID());
            }
            configurationNode.node("Gyms", gym.getIdentifier(), "BattleRules").set(battleRuleData);
            configurationNode.node("Gyms", gym.getIdentifier(), "DefaultTeam").set(gym.getDefaultTeamID());

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        try {
            clanGymConfig.getConfigLoader().save(configurationNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        clanGymCache.put(gym.getIdentifier(), gym);
    }

    public void saveAll(ClanGym gym)
    {
        File dir = Clandorus.clanGymStorageDir;
        File file = new File(dir, "%id%.json".replaceAll("%id%", String.valueOf(gym.getIdentifier())));
        Gson gson = Adapters.PRETTY_MAIN_GSON;
        JsonReader reader = null;
        try {
            reader = new JsonReader(new FileReader(file));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (reader == null) {
            Clandorus.log.error("Something went wrong attempting to read the Clan Gym Data");
            return;
        }


        try {
            FileWriter writer = new FileWriter(file);
            writer.write(gson.toJson(gym));
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        saveEntryToConfig(gym);
    }

    public boolean isClanGymNPC(Entity entity)
    {
        return entity.getPersistentData().getBoolean("clandorusGym");
    }

    @Nullable
    public ClanGym getGymFromNPC(Entity entity) {
        if (isClanGymNPC(entity)) {
            String id = entity.getPersistentData().getString("clandorusGymID");
            return clanGymCache.getOrDefault(id, null);
        }
        return null;
    }


}
