package io.github.adainish.clandorus.obj.gyms;

import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.OccupiedType;
import io.github.adainish.clandorus.obj.mail.Reward;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class OccupyingHolder {
    public UUID uuid;

    public OccupiedType occupiedType = OccupiedType.undefined;

    public long initialHoldingTime;

    public List<String> rewardIDs = new ArrayList<>();

    public OccupyingHolder(UUID uuid)
    {
        this.uuid = uuid;
        this.initialHoldingTime = System.currentTimeMillis();
    }

    /**
     * @author Winglet
     * Generates a list of rewards that'll be sent to the players mail box once claimed or when the occupation of the super object is ended
     */
    public void generateRewards()
    {
        switch (occupiedType)
        {
            case gym:
            {

                break;
            }
            case raid:
            {

                break;
            }
            default:
            {

                break;
            }
        }
        //timer handling through config as well as the ability to pull data directly from time calculation.
    }


    public List<Reward> rewards()
    {
        List<Reward> rewards = new ArrayList<>();
        for (String s:rewardIDs) {
            if (Clandorus.rewardRegistry.rewardCache.containsKey(s))
                rewards.add(Clandorus.rewardRegistry.rewardCache.get(s));
        }
        return rewards;
    }


    /**
     * @author Winglet
     * Returns a list of the actively stored rewards generated in the class
     * @return Reward objects
     */
    public List<Reward> getRewards()
    {
        return rewards();
    }
}
