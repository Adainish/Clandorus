package io.github.adainish.clandorus.tasks;

import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.clan.Clan;

import java.util.ArrayList;
import java.util.List;

public class UpdateClanDataTask implements Runnable{
    @Override
    public void run() {
        if (Clandorus.clanWrapper.clanCache.isEmpty())
            return;

        List<Clan> clanList = new ArrayList<>(Clandorus.clanWrapper.clanCache.values());
        for (int i = 0; i < clanList.size(); i++) {
            Clan clan = clanList.get(i);
            clan.save();
        }
    }
}
