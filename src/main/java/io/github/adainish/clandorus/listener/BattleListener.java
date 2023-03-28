package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.BeatTrainerEvent;
import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.enumeration.OccupiedType;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.obj.gyms.OccupyingHolder;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BattleListener
{

    @SubscribeEvent
    public void onImpactEvent(PokeBallImpactEvent event) {
        try {
            if (!event.getEntityHit().isPresent())
                return;

            if (event.getEntityHit().get() instanceof NPCTrainer) {
                if (Clandorus.clanGymRegistry.isClanGymNPC(event.getEntityHit().get()))
                {
                    ClanGym gym = Clandorus.clanGymRegistry.getGymFromNPC(event.getEntityHit().get());
                    if (gym == null)
                    {
                        Player player = PlayerStorage.getPlayer(event.getPokeBall().getOwnerId());
                        if (player != null)
                            player.sendMessage("&4&lSomething went wrong while loading the Clan Gym! Please contact a Staff Member!");
                        event.setCanceled(true);
                        return;
                    }
                    for (Pokemon p:StorageProxy.getParty(event.getPokeBall().getOwnerId()).getAll()) {
                        if (p != null) {
                            if (p.isEgg())
                                continue;
                            if (!gym.getHoldRequirements().isAllowed(p))
                            {
                                event.setCanceled(true);
                                Player player = PlayerStorage.getPlayer(event.getPokeBall().getOwnerId());
                                if (player != null)
                                    player.sendMessage("&cOne of your Pokemon is not allowed to be used in this gym!");
                                return;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Clandorus.log.error(e);
        }
    }
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
                        Clan clan = ClanStorage.getClan(player.getClanID());
                        if (clan != null) {
                            gym.getWinAction().executeWinAction(clan, player, pps.getTeam(), gym.getActiveHoldingPlayer(), event.trainer);
                            if (gym.getOccupyingHolder() != null)
                            {
                                gym.getOccupyingHolder().handoutRewards();
                            } else {
                                gym.setOccupyingHolder(new OccupyingHolder(event.player.getUniqueID()));
                                gym.getOccupyingHolder().occupiedType = OccupiedType.gym;
                            }
                            gym.setActiveHoldingPlayer(event.player.getUniqueID());
                            //update npc skin
                            gym.save();
                        }
                    }
                }
            }
        }
    }
}
