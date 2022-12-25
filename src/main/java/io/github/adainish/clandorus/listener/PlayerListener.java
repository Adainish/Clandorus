package io.github.adainish.clandorus.listener;

import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.Level;

public class PlayerListener {
    @SubscribeEvent
    public void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getPlayer() == null)
            return;

        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player == null) {
            PlayerStorage.makePlayer((ServerPlayerEntity) event.getPlayer());
            player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        }
        try {
            if (player == null)
                throw new NullPointerException("Player null even after making fresh player data? That's not good! Please contact the developer with more info!");
            player.setUserName(event.getPlayer().getName().getUnformattedComponentText());
            player.initialiseNull();
            player.savePlayer();
            if (player.inClan() && !player.clanCached()) {
                Clan clan = ClanStorage.getClan(player.getClanID());
                if (clan == null)
                    throw new NullPointerException("Clan Did not exist on player log-in even though the player data says they have a Team. This is an issue");
                clan.initialiseNullData();
                clan.save();
            }
        } catch (NullPointerException e) {
            Clandorus.log.log(Level.ERROR, e);
        }

    }


    @SubscribeEvent
    public void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getPlayer() == null)
            return;
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player == null) {
            PlayerStorage.makePlayer((ServerPlayerEntity) event.getPlayer());
            player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        }

        try {
            player.savePlayer();
        } catch (NullPointerException e) {

        }

    }
}
