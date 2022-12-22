package io.github.adainish.clandorus.wrapper;

import io.github.adainish.clandorus.obj.clan.Invite;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;

import java.util.ArrayList;
import java.util.List;

public class InviteWrapper {
    public List<Invite> inviteList = new ArrayList<>();

    public boolean hasBeenInvited(Player player, Clan clan) {
        for (Invite i:inviteList) {
            if (i.getClan().getClanIdentifier().equals(clan.getClanIdentifier()) && i.getInvited().getUuid().equals(player.getUuid()))
                return true;
        }
        return false;
    }


    public Invite getInvite(Clan clan, Player player) {
        for (Invite i:inviteList) {
            if (i.getClan().getClanIdentifier().equals(clan.getClanIdentifier()) && i.getInvited().getUuid().equals(player.getUuid()))
                return i;
        }
        return null;
    }
}
