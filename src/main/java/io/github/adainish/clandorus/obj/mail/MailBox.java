package io.github.adainish.clandorus.obj.mail;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import io.github.adainish.clandorus.enumeration.MailSender;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static io.github.adainish.clandorus.util.Util.filler;

public class MailBox {
    public List<Mail> mailList = new ArrayList<>();

    public MailBox()
    {

    }

    public void openMailBox(Player player)
    {
        ServerPlayerEntity playerEntity = Util.getPlayer(player.getUuid());
        UIManager.openUIForcefully(playerEntity, MailBoxPage(player));
    }

    public List<Button> mailRewardButtons(Player player)
    {
        List<Button> buttons = new ArrayList<>();
        for (Mail mail:mailList) {
            String title = "&9Mail";
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(title))
                    .lore(Util.formattedArrayList(Arrays.asList(mail.getMessage())))
                    .display(new ItemStack(PixelmonItems.pokemail_dream))
                    .onClick(b ->
                    {
                        UIManager.openUIForcefully(b.getPlayer(), mail.MailPage(player));
                    })
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage MailBoxPage(Player player)
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
        if (mailRewardButtons(player).size() > 8) {
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

        return PaginationHelper.createPagesFromPlaceholders(template, mailRewardButtons(player), LinkedPage.builder().title(Util.formattedString("&6MailBox")).template(template));
    }

    public Mail getMail(MailSender sender, UUID senderUUID, String message)
    {
        for (Mail m:mailList) {
            if (m.getSender().equals(sender) && m.getSenderUUID().equals(senderUUID) && m.getMessage().equals(message))
                return m;
        }
        return null;
    }

}
