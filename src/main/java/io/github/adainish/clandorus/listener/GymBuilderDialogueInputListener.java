package io.github.adainish.clandorus.listener;

import com.pixelmonmod.api.pokemon.PokemonSpecification;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.events.dialogue.DialogueInputEvent;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import io.github.adainish.clandorus.enumeration.GymBuilderAction;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.storage.PlayerStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class GymBuilderDialogueInputListener {
    @SubscribeEvent
    public void onDialogueScreenEvent(DialogueInputEvent.Submitted event) {
        Player player = PlayerStorage.getPlayer(event.getPlayer().getUniqueID());
        if (player != null) {
            if (player.getGymBuilder() != null) {
                if (player.getGymBuilder().getGymBuilderAction() != null) {
                    ClanGym gym = new ClanGym();
                    if (player.getGymBuilder().getGym() != null)
                        gym = player.getGymBuilder().getGym();
                    ClanGym finalGym = gym;
                    switch (player.getGymBuilder().getGymBuilderAction()) {
                        case title:
                        case title_error: {
                            if (event.getInput().isEmpty()) {
                                player.getGymBuilder().setGymBuilderAction(GymBuilderAction.title_error);
                                player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.title_error, player).sendTo(player.getServerEntity());
                                return;
                            }
                            finalGym.setIdentifier(event.getInput());
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);

                            Scheduling.schedule(2, () -> {
                                player.getGymBuilder().setGym(finalGym);
                                player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), finalGym);
                            }, false);
                            break;
                        }
                        case money_given_error:
                        case money_given: {
                            int am = 0;
                            try {
                                am = Integer.parseInt(event.getInput());
                            } catch (NumberFormatException e) {
                                player.getGymBuilder().setGymBuilderAction(GymBuilderAction.money_given_error);
                                player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.money_given_error, player).sendTo(player.getServerEntity());
                                return;
                            }
                            int finalAm = am;
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            finalGym.getWinAction().money = finalAm;

                            Scheduling.schedule(2, () -> {
                                player.getGymBuilder().setGym(finalGym);
                                player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), finalGym);
                            }, false);
                            break;
                        }
                        case hand_out_pokemon:
                        case hand_out_pokemon_error: {
                            if (event.getInput().isEmpty()) {
                                player.getGymBuilder().setGymBuilderAction(GymBuilderAction.hand_out_pokemon_error);
                                player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.hand_out_pokemon_error, player).sendTo(player.getServerEntity());
                                return;
                            } else {
                                PokemonSpecification spec = PokemonSpecificationProxy.create(event.getInput());
                                if (spec.create() == null || spec.create().getSpecies().equals(PixelmonSpecies.MISSINGNO.getValueUnsafe())) {
                                    player.getGymBuilder().setGymBuilderAction(GymBuilderAction.hand_out_pokemon_error);
                                    player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.hand_out_pokemon_error, player).sendTo(player.getServerEntity());
                                    return;
                                }
                            }
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            finalGym.getWinAction().pokemonSpecList.add(event.getInput());

                            Scheduling.schedule(2, () -> {
                                player.getGymBuilder().setGym(finalGym);
                                player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), finalGym);
                            }, false);
                            break;
                        }

                        case ban_spec:
                        case ban_spec_error: {
                            if (event.getInput().isEmpty()) {
                                player.getGymBuilder().setGymBuilderAction(GymBuilderAction.ban_spec_error);
                                player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.ban_spec_error, player).sendTo(player.getServerEntity());
                                return;
                            } else {
                                PokemonSpecification spec = PokemonSpecificationProxy.create(event.getInput());
                                if (spec.create() == null || spec.create().getSpecies().equals(PixelmonSpecies.MISSINGNO.getValueUnsafe())) {
                                    player.getGymBuilder().setGymBuilderAction(GymBuilderAction.ban_spec_error);
                                    player.getGymBuilder().dialogueInputScreenBuilder(GymBuilderAction.ban_spec_error, player).sendTo(player.getServerEntity());
                                    return;
                                }
                            }
                            player.getGymBuilder().setGymBuilderAction(GymBuilderAction.none);
                            if (!finalGym.getHoldRequirements().bannedPokemonSpecs.contains(event.getInput()))
                                finalGym.getHoldRequirements().bannedPokemonSpecs.add(event.getInput());
                            Scheduling.schedule(2, () -> {
                                player.getGymBuilder().setGym(finalGym);
                                player.getGymBuilder().openEditorUI(player, player.getGymBuilder().getNpcTrainer(), finalGym);
                            }, false);
                            break;
                        }
                        case none: {
                            break;
                        }
                        default:
                            return;
                    }
                }
            }
        }
    }
}
