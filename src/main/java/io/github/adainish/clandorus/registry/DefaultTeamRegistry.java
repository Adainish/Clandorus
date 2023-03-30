package io.github.adainish.clandorus.registry;

import com.pixelmonmod.pixelmon.api.util.helpers.RandomHelper;
import com.pixelmonmod.pixelmon.battles.api.rules.clauses.BattleClause;
import info.pixelmon.repack.org.spongepowered.CommentedConfigurationNode;
import info.pixelmon.repack.org.spongepowered.ConfigurateException;
import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.ClanGymConfig;
import io.github.adainish.clandorus.conf.DefaultTeamConfig;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.obj.gyms.DefaultTeam;

import java.util.*;

public class DefaultTeamRegistry
{
    public HashMap<String, DefaultTeam> defaultTeamHashMap = new HashMap<>();

    public DefaultTeamRegistry()
    {}

    public void load()
    {
        if (!defaultTeamHashMap.values().isEmpty())
        {
            for (DefaultTeam defaultTeam:defaultTeamHashMap.values()) {
                save(defaultTeam);
            }
            defaultTeamHashMap.clear();
        }
        Clandorus.log.warn("Loading Default Teams from config, this shouldn't take long!!");
        CommentedConfigurationNode node = DefaultTeamConfig.getConfig().get().node("DefaultTeams");
        Map<Object, CommentedConfigurationNode> nodeMap = node.childrenMap();
        for (Object obj : nodeMap.keySet()) {
            if (obj == null) {
                Clandorus.log.error("OBJ Null while generating Default Team");
                continue;
            }
            String identifier = obj.toString();

            if (defaultTeamHashMap.containsKey(identifier))
                continue;

            DefaultTeam defaultTeam = new DefaultTeam(identifier);
            defaultTeamHashMap.put(identifier, defaultTeam);
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
    public void save(DefaultTeam defaultTeam)
    {
        if (defaultTeam == null)
        {
            Clandorus.log.error("Attempted to save a default team that returned as null, please double check what you did!");
            return;
        }
        if (defaultTeam.identifier == null || defaultTeam.identifier.isEmpty())
        {
            defaultTeam.identifier = randomIDGenerator();
        }
        Clandorus.log.warn("Saving Default Team info to config");
        DefaultTeamConfig defaultTeamConfig = DefaultTeamConfig.getConfig();
        CommentedConfigurationNode configurationNode = defaultTeamConfig.get();
        try {

            List<String> specList = new ArrayList<>(defaultTeam.pokemonSpecs);
            configurationNode.node("DefaultTeams", defaultTeam.identifier, "PokemonSpecs").set(specList);

        } catch (SerializationException e) {
            throw new RuntimeException(e);
        }
        try {
            defaultTeamConfig.getConfigLoader().save(configurationNode);
        } catch (ConfigurateException e) {
            throw new RuntimeException(e);
        }
        defaultTeamHashMap.put(defaultTeam.identifier, defaultTeam);
    }
}
