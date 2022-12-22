package io.github.adainish.clandorus.obj.mail;

import io.github.adainish.clandorus.enumeration.MailSender;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Mail {
    public MailSender sender;

    public UUID senderUUID;

    public UUID targetUUID;

    public String message;
    public List<Reward> rewardList = new ArrayList<>();

    public boolean hasRewards()
    {
        return rewardList.isEmpty();
    }

    public void claimContents()
    {

    }
}
