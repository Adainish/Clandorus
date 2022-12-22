package io.github.adainish.clandorus.tasks;

import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.obj.clan.Invite;

import java.util.ArrayList;
import java.util.List;

public class UpdateInvitesTask implements Runnable{

    @Override
    public void run() {

        if (Clandorus.inviteWrapper.inviteList.isEmpty())
            return;

        List<Invite> toRemove = new ArrayList<>();

        for (Invite invite:Clandorus.inviteWrapper.inviteList) {
            if (invite == null)
                continue;

            if (invite.isExpiredInvite() || !invite.isOpenInvite() || invite.isInviteAccepted()) {
                toRemove.add(invite);
                continue;
            }

            if (invite.timer(invite) <= 0) {
                invite.setExpiredInvite(true);
                toRemove.add(invite);
            }
        }
        Clandorus.inviteWrapper.inviteList.removeAll(toRemove);
    }
}
