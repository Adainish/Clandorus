package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.enumeration.MailAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Objects;

public class MailBuilderDialogueInputListener {
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event)
    {
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null)
        {
            if (player.getMailBuilder() != null)
            {
                if (Objects.requireNonNull(player.getMailBuilder().getMailAction()) == MailAction.setting_mail_text) {
                    player.getMailBuilder().getMail().setMessage(event.getInput());
                    Scheduling.schedule(2, () -> {
                        player.getMailBuilder().setMailAction(MailAction.none);
                        player.getMailBuilder().openMailBuilder(player);
                    }, false);
                }
            }
        }
    }
}
