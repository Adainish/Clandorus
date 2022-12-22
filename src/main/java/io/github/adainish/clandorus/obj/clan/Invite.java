package io.github.adainish.clandorus.obj.clan;

import io.github.adainish.clandorus.conf.LanguageConfig;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class Invite {
    private Clan clan;
    private Player invited;
    private UUID invitee;
    private boolean openInvite;
    private boolean expiredInvite;
    private boolean inviteAccepted;
    private long inviteSentDate;
    private long maxExpiryTime;

    public Invite(Clan clan, Player invitedPlayer, UUID invitee) {
        this.setClan(clan);
        this.setInvited(invitedPlayer);
        this.setInvitee(invitee);
        this.setOpenInvite(true);
        this.setInviteSentDate(System.currentTimeMillis());
        this.setMaxExpiryTime(120);
    }

    public Clan getClan() {
        return clan;
    }

    public void setClan(Clan clan) {
        this.clan = clan;
    }

    public Player getInvited() {
        return invited;
    }

    public void setInvited(Player invited) {
        this.invited = invited;
    }

    public UUID getInvitee() {
        return invitee;
    }

    public void setInvitee(UUID invitee) {
        this.invitee = invitee;
    }

    public boolean isOpenInvite() {
        return openInvite;
    }

    public void setOpenInvite(boolean openInvite) {
        this.openInvite = openInvite;
    }

    public boolean isExpiredInvite() {
        return expiredInvite;
    }

    public void setExpiredInvite(boolean expiredInvite) {
        this.expiredInvite = expiredInvite;
        ServerPlayerEntity inviteePlayer = Util.getInstance().getPlayerList().getPlayerByUUID(invitee);
        ServerPlayerEntity invitedPlayer = Util.getInstance().getPlayerList().getPlayerByUUID(invited.getUuid());
        if (invitedPlayer != null)
            Util.send(invitedPlayer, LanguageConfig.getConfig().get().node("Invite", "ExpiredInvited").getString().replaceAll("%team%", clan.getClanName()));
        if (inviteePlayer != null)
            Util.send(inviteePlayer, LanguageConfig.getConfig().get().node("Invite", "ExpiredInvitee").getString().replaceAll("%invited%", Util.getPlayerName(invited.getUuid())));
    }

    public boolean isInviteAccepted() {
        return inviteAccepted;
    }

    public void setInviteAccepted(boolean inviteAccepted) {
        this.inviteAccepted = inviteAccepted;
    }

    public long getInviteSentDate() {
        return inviteSentDate;
    }

    public void setInviteSentDate(long inviteSentDate) {
        this.inviteSentDate = inviteSentDate;
    }

    public long getMaxExpiryTime() {
        return maxExpiryTime;
    }

    public void setMaxExpiryTime(long maxExpiryTime) {
        this.maxExpiryTime = maxExpiryTime;
    }

    public long timer(Invite invite) {
        return ((invite.getMaxExpiryTime()*1000 - (System.currentTimeMillis() - invite.getInviteSentDate())) / 1000);
    }
    public String timeLeftSeconds(Invite invite) {
        long timer = ((invite.getMaxExpiryTime() * 1000 - (System.currentTimeMillis() - invite.getInviteSentDate())) / 1000);
        return String.valueOf(TimeUnit.SECONDS.toSeconds(timer));
    }
    public String timeleftMinutes(Invite invite) {
        return String.valueOf(TimeUnit.SECONDS.toMinutes(timer(invite)));
    }

}
