package io.github.adainish.clandorus.listener;

import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.enumeration.GymBuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GymBuilderDialogueInputListener
{
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event)
    {
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null)
        {
            if (player.getGymBuilder() != null)
            {
                if (player.getGymBuilder().getGymBuilderAction() != null && player.getGymBuilder().getGymBuilderAction() == GymBuilderAction.title) {
                    ClanGym gym = new ClanGym();
                    gym.setIdentifier(event.getInput());
                    Scheduling.schedule(2, () -> {
                        player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                        player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), gym);
                    }, false);
                }
            }
        }
    }
}
