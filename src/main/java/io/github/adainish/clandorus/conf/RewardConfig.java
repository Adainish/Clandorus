package io.github.adainish.clandorus.conf;

import info.pixelmon.repack.org.spongepowered.serialize.SerializationException;
import io.github.adainish.clandorus.Clandorus;

import java.util.Arrays;

public class RewardConfig extends Configurable
{
    private static RewardConfig config;

    public static RewardConfig getConfig() {
        if (config == null) {
            config = new RewardConfig();
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
            this.get().node("Rewards", "Example", "Item").set("minecraft:paper");
            this.get().node("Rewards", "Example", "Title").set("&aExample");
            this.get().node("Rewards", "Example", "Lore").set(Arrays.asList("&bThis", "&7Is an example!"));
            this.get().node("Rewards", "Example", "Commands").set(Arrays.asList("broadcast &7Hi there!", "broadcast &bI am an example reward!"));
        } catch (SerializationException e) {
            Clandorus.log.error(e);
        }


    }

    public String getConfigName() {
        return "rewards.hocon";
    }

    public RewardConfig() {
    }
}
