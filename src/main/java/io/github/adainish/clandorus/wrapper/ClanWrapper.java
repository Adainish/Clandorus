package io.github.adainish.clandorus.wrapper;

import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.ClanStorage;

import java.util.HashMap;
import java.util.UUID;

public class ClanWrapper {

    public HashMap<UUID, Player> playerCache = new HashMap <>();
    public HashMap <UUID, Clan> clanCache = new HashMap <>();


    public ClanWrapper()
    {

    }

    public Clan getClanFromName(String name)
    {
        for (Clan clan: clanCache.values()) {
            if (clan.getClanName().equalsIgnoreCase(name))
                return clan;
        }
        return null;
    }

    public boolean isClanCached(UUID uuid) {
        return clanCache.containsKey(uuid);
    }

    public void addClanToCache(Clan clan) {
        clanCache.put(clan.getClanIdentifier(), clan);
    }

    public void removeClanFromCache(UUID uuid) {
        clanCache.remove(uuid);
    }

    public void safelyHandleTeamSaving(Clan clan) {
        ClanStorage.saveClan(clan);
    }
}
