package io.github.adainish.clandorus.obj.clan;

import io.github.adainish.clandorus.api.events.ClanEvent;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.util.Util;
import net.minecraftforge.common.MinecraftForge;

import java.util.UUID;

public class ClanChat {
    public String chatPrefix = "&6[ &bClan Chat &6] "; //set to default config options and create get/set methods for updating
    public String playerColour = "&e"; //set to default config options and create get/set methods for updating
    public String chatColour = "&b"; //set to default config options and create get/set methods for updating
    public ClanChat() {}
    public void sendClanMessage(Clan clan, Player sender, String msg) {
        if (sender.inClan()) {
            if (MinecraftForge.EVENT_BUS.post(new ClanEvent.ClanChatEvent(clan, sender, msg)))
            {
                String playerName = Util.getPlayerName(sender.getUuid());
                String chatMessage = chatPrefix + playerColour + playerName + " " + chatColour + msg;
                for (UUID uuid : clan.getClanMembers()) {
                    if (Util.isPlayerOnline(uuid)) {
                        Util.sendNoFormat(uuid, chatMessage);
                    }
                }
            }
        } else Util.send(sender.getUuid(), "&cYou're not currently in a clan, please first join a clan!");
    }
}
