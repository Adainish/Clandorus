package io.github.adainish.clandorus.conf;

import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;

import java.util.Arrays;

public class DefaultTeamConfig extends Configurable
{
    private static DefaultTeamConfig config;

    public static DefaultTeamConfig getConfig() {
        if (config == null) {
            config = new DefaultTeamConfig();
        }
        return config;
    }

    public void setup() {
        super.setup();
    }

    public void load() {
        super.load();
    }

    public void populate() {
        try {
            this.get().node("DefaultTeams", "Example", "PokemonSpecs").set(Arrays.asList("Rattata lvl:50", "totodile lvl:100"));
        } catch (SerializationException e) {
            Clandorus.log.error(e);
        }


    }

    public String getConfigName() {
        return "defaultteams.hocon";
    }

    public DefaultTeamConfig() {
    }

}
