package io.github.adainish.clandorus.api;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.LineType;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import io.github.adainish.clandorus.enumeration.GymBuilderAction;
import io.github.adainish.clandorus.enumeration.GymWinActions;
import io.github.adainish.clandorus.obj.Player;
import io.github.adainish.clandorus.obj.gyms.ClanGym;
import io.github.adainish.clandorus.obj.mail.Reward;
import io.github.adainish.clandorus.util.Util;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static io.github.adainish.clandorus.util.Util.filler;

public class GymBuilder
{
    private GymBuilderAction gymBuilderAction;
    private int entityID;
    private NPCTrainer npcTrainer;
    private ClanGym gym;

    public GymBuilder()
    {

    }

    public GymBuilder(ClanGym gym)
    {
        this.gym = gym;
    }

    public GymBuilder(NPCTrainer npcTrainer, ClanGym gym)
    {
        this.npcTrainer = npcTrainer;
        this.gym = gym;
    }

    public GymBuilder(NPCTrainer npcTrainer)
    {
        this.npcTrainer = npcTrainer;
    }

    public void updateGymBuilderCache(Player player) {
        player.setGymBuilder(this);
        player.updateCache();
    }

    public DialogueInputScreen.Builder dialogueInputScreenBuilder(GymBuilderAction action, Player player) {

        DialogueInputScreen.Builder builder = new DialogueInputScreen.Builder();
        builder.setShouldCloseOnEsc(false);
        switch (action) {
            case title: {
                builder.setTitle(Util.formattedString("&bGym Name"));
                builder.setText(Util.formattedString("&7Type the name you want to give this Gym"));
                break;
            }
            case title_error:
            {
                builder.setTitle(Util.formattedString("&4&lInvalid Name"));
                builder.setText(Util.formattedString("&cPlease provide a valid name!"));
                break;
            }
            case money_given:
            {
                builder.setTitle(Util.formattedString("&bSet Money"));
                builder.setText(Util.formattedString("&7Provide the amount of money that should be given"));
                break;
            }
            case hand_out_pokemon:
            {
                builder.setTitle(Util.formattedString("&b&lPokemon Spec"));
                builder.setText(Util.formattedString("&7&lProvide the Spec you want this Pokemon to have"));
                break;
            }
            case hand_out_pokemon_error:
            {
                builder.setTitle(Util.formattedString("&4&lInvalid Spec"));
                builder.setText(Util.formattedString("&cPlease provide a valid Pokemon Spec"));
                break;
            }
            case money_given_error:
            {
                builder.setTitle(Util.formattedString("&cInvalid integer"));
                builder.setText(Util.formattedString("&4Please provide a valid number!"));
                break;
            }
            case none: {
                break;
            }
        }
        setGymBuilderAction(action);
        updateGymBuilderCache(player);
        return builder;
    }

    public void openEditorUI(Player player, NPCTrainer trainer, ClanGym gym)
    {
        player.setGymBuilder(this);
        UIManager.openUIForcefully(Util.getPlayer(player.getUuid()), gym != null ? EditGymPage(player, gym, trainer) : CreateGymPage(player, trainer));
    }

    public List <Button> enabledRewardsButtons(Player player, NPCTrainer trainer, ClanGym gym)
    {
        List<Button> buttons = new ArrayList <>();
        for (Reward r:gym.getWinAction().returnRewardsFromIDs()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(r.getDisplayLore()))
                    .display(r.getDisplayItem())
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public LinkedPage ManageRewardsPage(Player player, ClanGym gym, NPCTrainer trainer)
    {
        PlaceholderButton placeHolderButton = new PlaceholderButton();
        Template template = null;
        template = ChestTemplate.builder(5)
                .border(0, 0, 5, 9, filler)
                .line(LineType.HORIZONTAL, 1, 1, 7, placeHolderButton)
                .build();

        return PaginationHelper.createPagesFromPlaceholders(template, enabledRewardsButtons(player, trainer, gym), LinkedPage.builder().title(Util.formattedString("&bGym Rewards")).template(template));
    }

    public List<String> getCurrentPokemonLore(NPCTrainer trainer)
    {
        List<String> stringList = new ArrayList <>();
        if (trainer.getPokemonStorage().getAll().length > 0)
        {
            for (Pokemon p:trainer.getPokemonStorage().getAll()) {
                if (p != null)
                    stringList.add(p.getSpecies().getName());
            }
        } else stringList.add("&7No Pokemon Detected");

        return stringList;
    }

    public ItemStack getActionStack(GymWinActions action)
    {
        Item item = null;
        switch (action)
        {
            case Reward:
            {
                item = PixelmonItems.gift_box;
                break;
            }
            case Give_Money:
            {
                item = PixelmonItems.amulet_coin;
                break;
            }
            case Give_Pokemon:
            {
                item = PixelmonItems.poke_ball;
                break;
            }
            case Take_Pokemon:
            {
                item = PixelmonItems.rocket_boots;
                break;
            }
            default:
            {
                item = Items.PAPER;
                break;
            }
        }

        return new ItemStack(item);
    }

    public List<Button> pokemonSpecButtonList(Player player, ClanGym gym, NPCTrainer trainer)
    {
        List<Button> buttons = new ArrayList <>();

        for (String s:gym.getWinAction().pokemonSpecList) {
            Pokemon p = gym.getWinAction().pokemonFromString(s);
            if (p != null)
            {
                GooeyButton button = GooeyButton.builder()
                        .title(Util.formattedString("&bPokemon Spec:"))
                        .lore(Util.formattedArrayList(Arrays.asList("&7" + s, "&4&lClick to remove")))
                        .display(SpriteItemHelper.getPhoto(p))
                        .onClick(b -> {
                            gym.getWinAction().pokemonSpecList.remove(s);
                            UIManager.openUIForcefully(b.getPlayer(), ManageSpecPage(player, gym, trainer));
                        })
                        .build();
                buttons.add(button);
            }
        }

        return buttons;
    }


    public LinkedPage ManageSpecPage(Player player, ClanGym gym, NPCTrainer trainer)
    {
        PlaceholderButton placeHolderButton = new PlaceholderButton();
        Template template = null;
        //back button
        template = ChestTemplate.builder(5)
                .border(0, 0, 5, 9, filler)
                .line(LineType.HORIZONTAL, 1, 1, 7, placeHolderButton)
                .build();

        return PaginationHelper.createPagesFromPlaceholders(template, pokemonSpecButtonList(player, gym, trainer), LinkedPage.builder().title(Util.formattedString("&bGym Rewards")).template(template));
    }

    public GooeyPage ManagePokemonPage(Player player, ClanGym gym, NPCTrainer trainer)
    {

        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);

        GooeyButton addSpec = GooeyButton.builder()
                .display(new ItemStack(Items.WRITABLE_BOOK))
                .title(Util.formattedString("&aAdd Spec"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to add a pokemon spec to give out to players")))
                .onClick(b -> {
                    dialogueInputScreenBuilder(GymBuilderAction.hand_out_pokemon, player).sendTo(b.getPlayer());
                })
                .build();

        GooeyButton viewAndDeleteSpecs = GooeyButton.builder()
                .title(Util.formattedString("&cManage Specs"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to view and remove configured pokemon specs", "&7Clicking on a spec will delete it")))
                .display(new ItemStack(Items.WRITTEN_BOOK))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageSpecPage(player, gym, trainer));
                })
                .build();

        builder.set(2, 3, viewAndDeleteSpecs);

        builder.set(2, 5, addSpec);

        return GooeyPage.builder().template(builder.build()).build();
    }

    public GooeyPage EditActionPage(Player player, ClanGym gym, NPCTrainer trainer)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        int i = 0;
        for (GymWinActions action: GymWinActions.values()) {
            List<String> lore = new ArrayList<>();
            switch (action)
            {
                case Reward:
                {
                    lore.add("&aClick to view and modify what rewards are available");
                    break;
                }
                case Give_Money:
                {
                    lore.add("&aClick to set the amount of money given upon taking a clan gym");
                    lore.add("&7The current amount is: %amount%$".replace("%amount%", String.valueOf(gym.getWinAction().money)));
                    break;
                }
                case Give_Pokemon:
                {
                    lore.add("&aClick to modify what Pokemon should be given upon beating a gym");
                    break;
                }
                case Take_Pokemon:
                {
                    lore.add("&aClick to set whether a pokemon can be stolen from an npc upon beating a gym");
                    lore.add("&aResulting in them being stolen from the defeated clan");
                    String enabled = gym.getWinAction().takePokemon ? "&a&lEnabled" : "&c&ldisabled";
                    lore.add("&7This option is %status%".replace("%status%", enabled));
                    break;
                }
            }
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&b%action%".replace("%action%", action.name().replaceAll("_", " "))))
                    .display(getActionStack(action))
                    .lore(Util.formattedArrayList(lore))
                    .onClick(b -> {
                        switch (action)
                        {
                            case Reward:
                            {
                                UIManager.openUIForcefully(b.getPlayer(), ManageRewardsPage(player, gym, trainer));
                                break;
                            }
                            case Give_Money:
                            {
                                dialogueInputScreenBuilder(GymBuilderAction.money_given, player).sendTo(player.getServerEntity());
                                break;
                            }
                            case Take_Pokemon:
                            {

                                gym.getWinAction().updateTakeStatus();
                                //update gym cache? need to decide on a "finish" option
                                UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player, gym, trainer));
                                break;
                            }
                            case Give_Pokemon:
                            {
                               //open menu where current pokemon can be reviewed / added / removed
                                UIManager.openUIForcefully(b.getPlayer(), ManagePokemonPage(player, gym, trainer));
                                // create args to give through specs
                                break;
                            }
                        }
                    })
                    .build();

            builder.set(2, i + 1, button);
            i++;
        }


        builder.border(0, 0, 6, 9, filler);


        return GooeyPage.builder().template(builder.build()).build();
    }


    public GooeyPage EditGymPage(Player player, ClanGym gym, NPCTrainer trainer)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        GooeyButton holder = GooeyButton.builder()
                .title(Util.formattedString("&7Current Holder: %holder%".replace("%holder%", gym.getHoldingPlayerName())))
                .display(new ItemStack(Items.SKELETON_SKULL))
                .onClick(b ->
                {

                })
                .build();

        GooeyButton defaultTeam = GooeyButton.builder()
                .title(Util.formattedString("&7Default Team: %team%".replace("%team%", gym.getDefaultTeamID())))
                .display(PokeBallRegistry.CHERISH_BALL.getValue().get().getBallItem())
                .onClick(b -> {
                    //go to selection page
                })
                .build();

        GooeyButton activeTeam = GooeyButton.builder()
                .title(Util.formattedString("&aActive team: "))
                .display(new ItemStack(PixelmonItems.poke_ball))
                .lore(Util.formattedArrayList(getCurrentPokemonLore(trainer)))
                .build();

        GooeyButton gymActions = GooeyButton.builder()
                .title(Util.formattedString("&eGym Actions"))
                .display(new ItemStack(Items.LEAD))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player, gym, trainer));
                })
                .build();

        GooeyButton holdRequirements = GooeyButton.builder()
                .title(Util.formattedString("&6Gym Rules and Requirements"))
                .display(new ItemStack(PixelmonItems.pokemon_editor))
                .onClick(b -> {
                    //go to holding requirement manager
                })
                .build();


        builder.border(0, 0, 6, 9, filler);

        builder.set(1, 1, holder);
        builder.set(1, 3, defaultTeam);
        builder.set(1, 5, activeTeam);
        builder.set(1, 7, gymActions);
        builder.set(2, 4, holdRequirements);

        return GooeyPage.builder().template(builder.build()).build();
    }

    public GooeyPage CreateGymPage(Player player, NPCTrainer trainer)
    {
        this.npcTrainer = trainer;

        GooeyButton createGymButton = GooeyButton.builder()
                .display(new ItemStack(Items.GREEN_DYE))
                .title(Util.formattedString("&aMake Clan Gym"))
                .onClick(b -> {
                    UIManager.closeUI(b.getPlayer());
                    player.setGymBuilder(this);
                    Scheduling.schedule(2, scheduledTask -> dialogueInputScreenBuilder(GymBuilderAction.title, player).sendTo(b.getPlayer()), false);
                })
                .build();

        GooeyButton cancelCreationButton = GooeyButton.builder()
                .display(new ItemStack(Items.RED_DYE))
                .title(Util.formattedString("&cCancel creation"))
                .onClick(b -> {
                    player.setGymBuilder(null);
                    player.updateCache();
                    UIManager.closeUI(b.getPlayer());
                })
                .build();

        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);
        builder.set(2, 3, cancelCreationButton);
        builder.set(2, 5, createGymButton);


        return GooeyPage.builder().template(builder.build()).build();
    }

    public GymBuilderAction getGymBuilderAction() {
        return gymBuilderAction;
    }

    public void setGymBuilderAction(GymBuilderAction gymBuilderAction) {
        this.gymBuilderAction = gymBuilderAction;
    }

    public int getEntityID() {
        return entityID;
    }

    public void setEntityID(int entityID) {
        this.entityID = entityID;
    }

    public NPCTrainer getNpcTrainer() {
        return npcTrainer;
    }

    public void setNpcTrainer(NPCTrainer npcTrainer) {
        this.npcTrainer = npcTrainer;
    }

    public ClanGym getGym() {
        return gym;
    }

    public void setGym(ClanGym gym) {
        this.gym = gym;
    }
}
