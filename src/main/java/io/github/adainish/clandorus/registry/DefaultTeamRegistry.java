package io.github.adainish.clandorus.registry;

import info.pixelmon.repack.org.spongepowered.CommentedConfigurationNode;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.conf.ClanGymConfig;
import io.github.adainish.clandorus.conf.DefaultTeamConfig;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.obj.gyms.DefaultTeam;

import java.util.HashMap;
import java.util.Map;

public class DefaultTeamRegistry
{
    public HashMap<String, DefaultTeam> defaultTeamHashMap = new HashMap<>();

    public DefaultTeamRegistry()
    {}

    public void load()
    {
        if (!defaultTeamHashMap.values().isEmpty())
        {
            //save and remove
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
}
