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
                if (player.getGymBuilder().getGymBuilderAction() != null)
                {
                    ClanGym gym = new ClanGym();
                    if (player.getGymBuilder().getGym() != null)
                        gym = player.getGymBuilder().getGym();
                    ClanGym finalGym = gym;
                    switch (player.getGymBuilder().getGymBuilderAction())
                    {
                        case title: {
                            finalGym.setIdentifier(event.getInput());
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            break;
                        }
                        case money_given_error:
                        case money_given: {
                            int am = 0;
                            try {
                                am = Integer.parseInt(event.getInput());
                            } catch (NumberFormatException e)
                            {
                                player.getGymBuilder().setGymBuilderAction(GymBuilderAction.money_given_error);
                                player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.money_given_error, player).sendTo(player.getServerEntity());
                                return;
                            }
                            int finalAm = am;
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            finalGym.getWinAction().money = finalAm;
                            break;
                        }
                        case hand_out_pokemon:
                        {
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            //transform to be different
                            player.getGymBuilder().getGym().getWinAction().givePokemon.add(event.getInput());
                            break;
                        }
                        case none:
                        {
                            break;
                        }
                        default:
                            return;
                    }

                    Scheduling.schedule(2, () -> {
                        player.getGymBuilder().setGym(finalGym);
                        player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), finalGym);
                    }, false);
                }
            }
        }
    }
}
