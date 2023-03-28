package io.github.adainish.clandorus.conf;

import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;

import java.util.Arrays;

public class ClanGymConfig extends Configurable
{
    private static ClanGymConfig config;

    public static ClanGymConfig getConfig() {
        if (config == null) {
            config = new ClanGymConfig();
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
            this.get().node("Gyms", "Example", "Location", "WorldID").set("world");
            this.get().node("Gyms", "Example", "Location", "X").set(100D);
            this.get().node("Gyms", "Example", "Location", "Y").set(100D);
            this.get().node("Gyms", "Example", "Location", "Z").set(100D);
            this.get().node("Gyms", "Example", "HoldRequirements", "BannedPokemonSpecs").set(Arrays.asList());
            this.get().node("Gyms", "Example", "HoldRequirements", "BannedAbilities").set(Arrays.asList());
            this.get().node("Gyms", "Example", "HoldRequirements", "BannedAttacks").set(Arrays.asList());
            this.get().node("Gyms", "Example", "HoldRequirements", "MinIVS").set(0);
            this.get().node("Gyms", "Example", "WinAction", "RewardIDs").set(Arrays.asList(""));
            this.get().node("Gyms", "Example", "WinAction", "TakePokemon").set(false);
            this.get().node("Gyms", "Example", "WinAction", "PokemonSpecs").set(Arrays.asList(""));
            this.get().node("Gyms", "Example", "WinAction", "Money").set(0);

            this.get().node("Gyms", "Example", "BattleRules").set(Arrays.asList(""));
            this.get().node("Gyms", "Example", "DefaultTeam").set("");
        } catch (SerializationException e) {
            Clandorus.log.error(e);
        }


    }

    public String getConfigName() {
        return "clangyms.hocon";
    }

    public ClanGymConfig() {
    }

}
