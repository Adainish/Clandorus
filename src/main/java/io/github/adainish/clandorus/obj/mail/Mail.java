package io.github.adainish.clandorus.obj.mail;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import io.github.adainish.clandorus.enumeration.MailSender;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.clan.Clan;
import io.github.adainish.clandorus.storage.ClanStorage;
import io.github.adainish.clandorus.storage.PlayerStorage;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.github.adainish.clandorus.util.Util.filler;

public class Mail {
    private MailSender sender;

    private UUID senderUUID;

    private UUID targetUUID;

    private String message;
    private List<Reward> rewardList = new ArrayList<>();

    public boolean hasRewards()
    {
        return !getRewardList().isEmpty();
    }

    public List<Button> mailRewardButtons()
    {
        List<Button> buttons = new ArrayList<>();
        for (Reward reward: getRewardList()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(reward.getDisplayTitle()))
                    .lore(Util.formattedArrayList(reward.getDisplayLore()))
                    .display(reward.getDisplayItem())
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage RewardPage(Player player)
    {
        PlaceholderButton placeHolderButton = new PlaceholderButton();
        LinkedPageButton previous = LinkedPageButton.builder()
                .display(new ItemStack(PixelmonItems.trade_holder_left))
                .title("Previous Page")
                .linkType(LinkType.Previous)
                .build();

        LinkedPageButton next = LinkedPageButton.builder()
                .display(new ItemStack(PixelmonItems.trade_holder_right))
                .title("Next Page")
                .linkType(LinkType.Next)
                .build();

        Template template;
        if (mailRewardButtons().size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, mailRewardButtons(), LinkedPage.builder().title(Util.formattedString("&aMail")).template(template));
    }

    public String retrieveMailSenderName()
    {
        String s = "&7Mail from: &b";
        StringBuilder stringBuilder = new StringBuilder(s);
        if (sender == null)
        {
            stringBuilder.append("NO SENDER FOUND");
            return stringBuilder.toString();
        }
        switch (sender) {
            case Clan: {
                Clan clan = ClanStorage.getClan(senderUUID);
                if (clan != null)
                    stringBuilder.append(clan.getClanName());
                else stringBuilder.append("&4Clan no longer exists");
                break;
            }
            case Player: {
                Player player = PlayerStorage.getPlayer(senderUUID);
                if (player != null)
                    stringBuilder.append(player.getName());
                else stringBuilder.append("&4Player no longer exists");
                break;
            }
            case Server: {
                stringBuilder.append("Server");
                break;
            }
            default:
                stringBuilder.append(" NO SENDER FOUND");
        }
        if (senderUUID == null) {
            stringBuilder.append("NO SENDER FOUND");
        }
        return stringBuilder.toString();
    }

    public GooeyPage MailPage(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(4);
        builder.fill(filler);

        GooeyButton claimContent = GooeyButton.builder()
                .title(Util.formattedString("&5Claim Rewards"))
                .display(new ItemStack(PixelmonItems.amulet_coin))
                .onClick(b ->
                {
                    UIManager.closeUI(b.getPlayer());
                    claimContents(player);
                })
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to claim the rewards from this mail")))
                .build();

        GooeyButton viewRewards = GooeyButton.builder()
                .title(Util.formattedString("&6View Rewards"))
                .display(new ItemStack(PixelmonItems.gift_box))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), RewardPage(player));
                })
                .build();

        GooeyButton mailInfo = GooeyButton.builder()
                .title(Util.formattedString(retrieveMailSenderName()))
                .display(new ItemStack(Items.ENCHANTED_BOOK))
                .lore(Util.formattedArrayList(Arrays.asList(message)))
                .build();

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&7Go Back"))
                .display(new ItemStack(Items.ARROW))
                .onClick(b ->
                {
                    UIManager.openUIForcefully(b.getPlayer(), player.getMailBox().MailBoxPage(player));
                })
                .build();


        if (hasRewards()) {
            builder.set(1, 1, back);
            builder.set(1, 3, mailInfo);
            builder.set(1, 5, viewRewards);
            builder.set(1, 7, claimContent);
        } else {
            builder = ChestTemplate.builder(3);
            builder.fill(filler);
            builder.set(1, 3, back);
            builder.set(1, 5, mailInfo);
        }
        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&6Mail")).build();
    }

    public void claimContents(Player player)
    {
        for (Reward reward: getRewardList()) {
            reward.handOutRewards(player);
        }
        player.getMailBox().getMail(getSender(), getSenderUUID(), getMessage()).getRewardList().clear();
        player.savePlayer();
    }

    public MailSender getSender() {
        return sender;
    }

    public void setSender(MailSender sender) {
        this.sender = sender;
    }

    public UUID getSenderUUID() {
        return senderUUID;
    }

    public void setSenderUUID(UUID senderUUID) {
        this.senderUUID = senderUUID;
    }

    public UUID getTargetUUID() {
        return targetUUID;
    }

    public void setTargetUUID(UUID targetUUID) {
        this.targetUUID = targetUUID;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public List<Reward> getRewardList() {
        return rewardList;
    }

    public void setRewardList(List<Reward> rewardList) {
        this.rewardList = rewardList;
    }
}
