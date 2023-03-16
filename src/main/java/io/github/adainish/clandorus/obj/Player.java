package io.github.adainish.clandorus.obj;

import com.pixelmonmod.pixelmon.api.storage.PCStorage;
import com.pixelmonmod.pixelmon.api.storage.PlayerPartyStorage;
import com.pixelmonmod.pixelmon.api.storage.StorageProxy;
import io.github.adainish.clandorus.Clandorus;
import io.github.adainish.clandorus.api.GymBuilder;
import io.github.adainish.clandorus.api.MailBuilder;
import io.github.adainish.clandorus.api.RewardBuilder;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.obj.mail.MailBox;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import org.apache.logging.log4j.Level;

import java.util.UUID;

public class Player {
    private UUID uuid;
    private String userName;
    private UUID clanID;

    private MailBox mailBox;

    private transient GymBuilder gymBuilder;

    private transient RewardBuilder rewardBuilder;

    private transient MailBuilder mailBuilder;

    public Player(UUID uuid)
    {
        setUuid(uuid);
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public UUID getClanID() {
        return clanID;
    }

    public void setClanID(UUID clanID) {
        this.clanID = clanID;
    }

    public boolean clanCached() {
        return Clandorus.clanWrapper.isClanCached(clanID);
    }

    public void sendMessage(String message)
    {
        Util.send(uuid, message);
    }

    public void initialiseNull()
    {
        if (getMailBox() == null) {
            setMailBox(new MailBox());
        }
    }

    public boolean inClan() {
        if (clanID == null)
            return false;
        Clan clan = ClanStorage.getClan(clanID);
        if (clan == null) {
            try {
                throw new Exception("Clan did not exist in storage, this specific error should not be capable of appearing. Please contact the dev with this message to have your storage handling checked!");
            } catch (Exception e) {
                Clandorus.log.log(Level.ERROR, e);
            }
            return false;

        }
        return clan.getClanMembers().contains(uuid);
    }


    public PCStorage getPixelmonComputerStorage()
    {
        return StorageProxy.getPCForPlayer(uuid);
    }

    public PlayerPartyStorage getPixelmonPartyStorage()
    {
        return StorageProxy.getParty(uuid);
    }

    public ServerPlayerEntity getServerEntity()
    {
        return Util.getPlayer(uuid);
    }

    public void updateCache() {
        if (Clandorus.clanWrapper.playerCache.containsKey(this.getUuid()))
            Clandorus.clanWrapper.playerCache.replace(this.getUuid(), this);
        else Clandorus.clanWrapper.playerCache.put(this.getUuid(), this);
    }

    public void savePlayer() {
        PlayerStorage.savePlayer(this);
    }

    public MailBox getMailBox() {
        return mailBox;
    }

    public void setMailBox(MailBox mailBox) {
        this.mailBox = mailBox;
    }

    public RewardBuilder getRewardBuilder() {
        return rewardBuilder;
    }

    public void setRewardBuilder(RewardBuilder rewardBuilder) {
        this.rewardBuilder = rewardBuilder;
    }

    public MailBuilder getMailBuilder() {
        return mailBuilder;
    }

    public void setMailBuilder(MailBuilder mailBuilder) {
        this.mailBuilder = mailBuilder;
    }

    public GymBuilder getGymBuilder() {
        return gymBuilder;
    }

    public void setGymBuilder(GymBuilder gymBuilder) {
        this.gymBuilder = gymBuilder;
    }
}
