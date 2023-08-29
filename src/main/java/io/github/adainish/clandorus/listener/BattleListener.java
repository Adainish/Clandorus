package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.enums.BattleEndTaskType;
import com.pixelmonmod.pixelmon.api.events.BeatTrainerEvent;
import com.pixelmonmod.pixelmon.api.events.PokeBallImpactEvent;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.UUID;

public class BattleListener
{

    @SubscribeEvent
    public void onImpactEvent(PokeBallImpactEvent event) {
        try {
            if (!event.getEntityHit().isPresent())
                return;

            if (event.getEntityHit().get() instanceof NPCTrainer) {
                if (Clandorus.clanGymRegistry.isClanGymNPC(event.getEntityHit().get())) {
                    ClanGym gym = Clandorus.clanGymRegistry.getGymFromNPC(event.getEntityHit().get());
                    if (gym == null) {
                        Player player = PlayerStorage.getPlayer(event.getPokeBall().getOwnerId());
                        if (player != null)
                            player.sendMessage("&4&lSomething went wrong while loading the Clan Gym! Please contact a Staff Member!");
                        event.setCanceled(true);
                        return;
                    }
                    Player player = PlayerStorage.getPlayer(event.getPokeBall().getOwnerId());
                    if (player != null) {
                        if (player.inClan()) {
                            if (gym.getOccupyingHolder() != null && event.getPokeBall().getOwnerId().equals(gym.getOccupyingHolder().uuid)) {
                                player.sendMessage("&cYou're already the active holder of this gym!");
                                event.setCanceled(true);
                                return;
                            }
                            if (player.getClanOptional().isPresent()) {
                                Clan clan = player.getClanOptional().get();
                                UUID holder = gym.getOccupyingHolder().uuid;
                                if (holder != null)
                                {
                                    if (clan.isMember(holder))
                                    {
                                        player.sendMessage("&cYou can't challenge a Clan Gym that's being held by your Clan!");
                                        event.setCanceled(true);
                                        return;
                                    }
                                }
                                for (Pokemon p : StorageProxy.getParty(event.getPokeBall().getOwnerId()).getAll()) {
                                    if (p != null) {
                                        if (p.isEgg())
                                            continue;
                                        if (!gym.getHoldRequirements().isAllowed(p)) {
                                            event.setCanceled(true);
                                            player.sendMessage("&cOne of your Pokemon is not allowed to be used in this gym!");
                                            return;
                                        }
                                    }
                                }
                            } else {
                                event.setCanceled(true);
                                Util.send(event.getPokeBall().getOwnerId(), "&cYou're currently not allowed to participate in Clan Gyms! You need to join or create a clan to play with this gamemode!");
                            }
                        } else {
                            event.setCanceled(true);
                            Util.send(event.getPokeBall().getOwnerId(), "&cYou're currently not allowed to participate in Clan Gyms! You need to join or create a clan to play with this gamemode!");
                        }

                    } else {
                        event.setCanceled(true);
                        Util.send(event.getPokeBall().getOwnerId(), "&cFailed to load your player data, please contact a staff member");
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
            Player player = PlayerStorage.getPlayer(event.player.getUniqueID());
            if (gym != null) {
                if (player != null) {
                    if (player.inClan()) {
                        Clan clan = ClanStorage.getClan(player.getClanID());
                        if (clan != null) {
                            StorageProxy.getParty(event.player.getUniqueID()).addTaskForBattleEnd(BattleEndTaskType.ALWAYS_QUEUE, battleController -> {
                                    gym.getWinAction().executeWinAction(gym, clan, player, gym.getActiveHoldingPlayer(), event.trainer);
                            });
                        }
                    }
                }
            }
        }
    }
}
