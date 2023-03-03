package io.github.adainish.clandorus.obj;

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
    public List<Reward> rewards = new ArrayList<>();

    public OccupyingHolder(UUID uuid)
    {
        this.uuid = uuid;
        initialHoldingTime = System.currentTimeMillis();
    }

    /**
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

    /**
     * Returns a list of the actively stored rewards generated in the class
     * @return Reward objects
     */
    public List<Reward> getRewards()
    {
        return rewards;
    }
}
