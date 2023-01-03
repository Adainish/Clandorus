package io.github.adainish.clandorus.registry;

import io.github.adainish.clandorus.obj.gyms.ClanGym;
import net.minecraft.entity.Entity;

import javax.annotation.Nullable;
import java.util.HashMap;

public class ClanGymRegistry {

    public HashMap<String, ClanGym> clanGymCache = new HashMap<>();


    public void loadFromStorage()
    {

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
