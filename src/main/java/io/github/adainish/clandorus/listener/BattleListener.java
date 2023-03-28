package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.BeatTrainerEvent;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BattleListener
{

    @SubscribeEvent
    public void onBeatTrainerEvent(BeatTrainerEvent event)
    {
        if (event.isCanceled())
            return;

        if (Clandorus.clanGymRegistry.isClanGymNPC(event.trainer)) {
            ClanGym gym = Clandorus.clanGymRegistry.getGymFromNPC(event.trainer);
            PlayerPartyStorage pps = StorageProxy.getParty(event.player.getUniqueID());
            Player player = PlayerStorage.getPlayer(event.player.getUniqueID());
            if (gym != null) {
                if (player != null) {
                    if (player.inClan()) {
                        Clan clan = ClanStorage.getClan(player.getUuid());
                        if (clan != null) {
                            gym.getWinAction().executeWinAction(clan, player, pps.getTeam(), gym.getActiveHoldingPlayer());
                            gym.save();
                        }
                    }
                }
            }
        }
    }
}
