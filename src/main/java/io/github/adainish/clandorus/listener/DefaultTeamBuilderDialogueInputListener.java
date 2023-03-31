package io.github.adainish.clandorus.listener;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.enumeration.DefaultTeamBuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class DefaultTeamBuilderDialogueInputListener
{
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event)
    {
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null) {
            if (player.getDefaultTeamBuilder() != null) {
                String input = event.getInput();
                if (player.getDefaultTeamBuilder().getTeamBuilderAction() != null) {
                    switch (player.getDefaultTeamBuilder().getTeamBuilderAction()) {
                        case specs_error:
                        case specs: {
                            if (input.isEmpty()) {
                                player.getDefaultTeamBuilder().setTeamBuilderAction(DefaultTeamBuilderAction.specs_error);
                                player.getDefaultTeamBuilder().dialogueInputScreenBuilder(DefaultTeamBuilderAction.specs_error, player).sendTo(player.getServerEntity());
                            } else {
                                PokemonSpecification spec = PokemonSpecificationProxy.create(input);
                                if (spec.create() == null || spec.create().getSpecies().equals(PixelmonSpecies.MISSINGNO.getValueUnsafe())) {
                                    player.getDefaultTeamBuilder().setTeamBuilderAction(DefaultTeamBuilderAction.specs_error);
                                    player.getDefaultTeamBuilder().dialogueInputScreenBuilder(DefaultTeamBuilderAction.specs_error, player).sendTo(player.getServerEntity());
                                    return;
                                }

                                player.getDefaultTeamBuilder().setTeamBuilderAction(DefaultTeamBuilderAction.none);
                                player.getDefaultTeamBuilder().getDefaultTeam().pokemonSpecs.add(input);

                                Scheduling.schedule(2, () -> {
                                    player.getDefaultTeamBuilder().openUI(player, player.getDefaultTeamBuilder().getDefaultTeam());
                                }, false);
                            }
                            break;
                        }
                        case none:
                        default: {

                            break;
                        }
                    }
                }
            }
        }
    }
}
