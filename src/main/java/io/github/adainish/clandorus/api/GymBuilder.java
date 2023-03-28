package io.github.adainish.clandorus.api;

import ca.landonjw.gooeylibs2.api.UIManager;
import ca.landonjw.gooeylibs2.api.button.Button;
import ca.landonjw.gooeylibs2.api.button.GooeyButton;
import ca.landonjw.gooeylibs2.api.button.PlaceholderButton;
import ca.landonjw.gooeylibs2.api.button.linked.LinkType;
import ca.landonjw.gooeylibs2.api.button.linked.LinkedPageButton;
import ca.landonjw.gooeylibs2.api.helpers.PaginationHelper;
import ca.landonjw.gooeylibs2.api.page.GooeyPage;
import ca.landonjw.gooeylibs2.api.page.LinkedPage;
import ca.landonjw.gooeylibs2.api.template.LineType;
import ca.landonjw.gooeylibs2.api.template.Template;
import ca.landonjw.gooeylibs2.api.template.types.ChestTemplate;
import com.pixelmonmod.api.pokemon.PokemonSpecificationProxy;
import com.pixelmonmod.pixelmon.api.battles.attack.AttackRegistry;
import com.pixelmonmod.pixelmon.api.dialogue.DialogueInputScreen;
import com.pixelmonmod.pixelmon.api.pokemon.Pokemon;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonBuilder;
import com.pixelmonmod.pixelmon.api.pokemon.PokemonFactory;
import com.pixelmonmod.pixelmon.api.pokemon.ability.Ability;
import com.pixelmonmod.pixelmon.api.pokemon.ability.AbilityRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.item.pokeball.PokeBallRegistry;
import com.pixelmonmod.pixelmon.api.pokemon.species.Species;
import com.pixelmonmod.pixelmon.api.pokemon.species.Stats;
import com.pixelmonmod.pixelmon.api.pokemon.species.abilities.Abilities;
import com.pixelmonmod.pixelmon.api.registries.PixelmonItems;
import com.pixelmonmod.pixelmon.api.registries.PixelmonSpecies;
import com.pixelmonmod.pixelmon.api.util.Scheduling;
import com.pixelmonmod.pixelmon.api.util.helpers.SpriteItemHelper;
import com.pixelmonmod.pixelmon.battles.attacks.ImmutableAttack;
import com.pixelmonmod.pixelmon.blocks.PixelmonBlock;
import com.pixelmonmod.pixelmon.entities.npcs.NPCTrainer;
import com.pixelmonmod.pixelmon.enums.TMType;
import com.pixelmonmod.pixelmon.enums.technicalmoves.*;
import com.pixelmonmod.pixelmon.items.PixelmonBlockItem;
import io.github.adainish.clandorus.Clandorus;
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
import java.util.Comparator;
import java.util.List;

import static io.github.adainish.clandorus.util.Util.filler;

public class GymBuilder {
    private GymBuilderAction gymBuilderAction;
    private int entityID;
    private NPCTrainer npcTrainer;
    private ClanGym gym;

    public GymBuilder() {

    }

    public GymBuilder(ClanGym gym) {
        this.gym = gym;
    }

    public GymBuilder(NPCTrainer npcTrainer, ClanGym gym) {
        this.npcTrainer = npcTrainer;
        this.gym = gym;
    }

    public GymBuilder(NPCTrainer npcTrainer) {
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
            case title_error: {
                builder.setTitle(Util.formattedString("&4&lInvalid Name"));
                builder.setText(Util.formattedString("&cPlease provide a valid name!"));
                break;
            }
            case money_given: {
                builder.setTitle(Util.formattedString("&bSet Money"));
                builder.setText(Util.formattedString("&7Provide the amount of money that should be given"));
                break;
            }
            case hand_out_pokemon: {
                builder.setTitle(Util.formattedString("&b&lPokemon Spec"));
                builder.setText(Util.formattedString("&7&lProvide the Spec you want this Pokemon to have"));
                break;
            }
            case hand_out_pokemon_error:
            case ban_spec_error: {
                builder.setTitle(Util.formattedString("&4&lInvalid Spec"));
                builder.setText(Util.formattedString("&cPlease provide a valid Pokemon Spec"));
                break;
            }
            case ban_spec: {
                builder.setTitle(Util.formattedString("&b&lPokemon Spec"));
                builder.setText(Util.formattedString("&7&lProvide the Spec you want to ban"));
                break;
            }
            case money_given_error: {
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

    public void openEditorUI(Player player, NPCTrainer trainer, ClanGym gym) {
        player.setGymBuilder(this);
        if (gym != null)
            this.gym = gym;
        if (trainer != null)
            this.npcTrainer = trainer;
        UIManager.openUIForcefully(Util.getPlayer(player.getUuid()), gym != null ? EditGymPage(player) : CreateGymPage(player, trainer));
    }


    public List <Button> selectedRewards(Player player) {
        List <Button> buttons = new ArrayList <>();

        for (Reward r : this.gym.getWinAction().returnRewardsFromIDs()) {
            List <String> lore = new ArrayList <>();
            lore.add("&7Click to remove this reward");
            lore.addAll(r.getDisplayLore());
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(lore))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        this.gym.getWinAction().rewardIDs.remove(r.getIdentifier());
                        openEditorUI(player, this.npcTrainer, this.gym);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }


    public List <Button> selectableRewards(Player player) {
        List <Button> buttons = new ArrayList <>();

        for (Reward r : Clandorus.rewardRegistry.rewardCache.values()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(r.getDisplayLore()))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        this.gym.getWinAction().rewardIDs.add(r.getIdentifier());
                        openEditorUI(player, this.npcTrainer, this.gym);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }

    public List <Button> viewableRewards(Player player) {
        List <Button> buttons = new ArrayList <>();

        for (Reward r : player.getGymBuilder().gym.getWinAction().returnRewardsFromIDs()) {
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString(r.getDisplayTitle()))
                    .lore(Util.formattedArrayList(r.getDisplayLore()))
                    .display(r.getDisplayItem())
                    .onClick(b ->
                    {
                        player.getGymBuilder().openEditorUI(player, this.npcTrainer, this.gym);
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }


    public LinkedPage RemoveSelectedRewardsMenu(Player player) {
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

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RewardManageMenu(player));
                })
                .build();


        Template template;

        if (selectedRewards(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .set(0, 0, back)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                selectedRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Remove Rewards"))
                        .template(template));
    }

    public LinkedPage SelectableRewardsMenu(Player player) {
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


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RewardManageMenu(player));
                })
                .build();


        Template template;

        if (selectableRewards(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 5, next)
                    .set(0, 0, back)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                selectableRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Selectable Rewards"))
                        .template(template));
    }

    public LinkedPage ViewableRewardsMenu(Player player) {
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


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RewardManageMenu(player));
                })
                .build();


        Template template;

        if (viewableRewards(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 0, back)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(
                template,
                viewableRewards(player),
                LinkedPage
                        .builder()
                        .title(Util.formattedString("&6Viewable Rewards"))
                        .template(template));
    }

    public GooeyPage RewardManageMenu(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(3);
        builder.fill(filler);

        GooeyButton clearAll = GooeyButton.builder()
                .display(new ItemStack(Items.LAVA_BUCKET))
                .onClick(b ->
                {
                    this.gym.getWinAction().rewardIDs.clear();
                    openEditorUI(player, this.npcTrainer, this.gym);
                })
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove &4&lall &7Rewards from the Gym")))
                .title(Util.formattedString("&6Remove All"))
                .build();

        GooeyButton selectRewards = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Select Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to select a Reward from the Reward Registry")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), SelectableRewardsMenu(player));
                })
                .build();


        GooeyButton viewRewards = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6View Rewards"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to view all rewards you've selected for this Gym")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ViewableRewardsMenu(player));
                })
                .build();


        GooeyButton removeReward = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Remove Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to remove a Reward you've selected for this Gym")))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), RemoveSelectedRewardsMenu(player));
                })
                .build();

        GooeyButton makeReward = GooeyButton.builder()
                .display(new ItemStack(Items.BOOK))
                .title(Util.formattedString("&6Build a Reward"))
                .lore(Util.formattedArrayList(Arrays.asList("&7Click to build a new reward and add it to the Gym")))
                .onClick(b -> {
                    player.setGymBuilder(this);
                    player.setRewardBuilder(new RewardBuilder());
                    player.getRewardBuilder().openNewRewardBuilder(b.getPlayer());
                })
                .build();


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player));
                })
                .build();


        builder.set(1, 0, viewRewards);
        builder.set(1, 2, makeReward);
        builder.set(1, 4, selectRewards);
        builder.set(1, 6, removeReward);
        builder.set(1, 8, clearAll);
        builder.set(2, 0, back);

        return GooeyPage.builder().template(builder.build()).title(Util.formattedString("&6Manage Rewards")).build();
    }

    public List <String> getCurrentPokemonLore(NPCTrainer trainer) {
        List <String> stringList = new ArrayList <>();
        if (trainer.getPokemonStorage().getAll().length > 0) {
            for (Pokemon p : trainer.getPokemonStorage().getAll()) {
                if (p != null)
                    stringList.add(p.getSpecies().getName());
            }
        } else stringList.add("&7No Pokemon Detected");

        return stringList;
    }

    public ItemStack getActionStack(GymWinActions action) {
        Item item = null;
        switch (action) {
            case Reward: {
                item = PixelmonItems.gift_box;
                break;
            }
            case Give_Money: {
                item = PixelmonItems.amulet_coin;
                break;
            }
            case Give_Pokemon: {
                item = PixelmonItems.poke_ball;
                break;
            }
            case Take_Pokemon: {
                item = PixelmonItems.rocket_boots;
                break;
            }
            default: {
                item = Items.PAPER;
                break;
            }
        }

        return new ItemStack(item);
    }

    public List <Button> pokemonSpecButtonList(Player player) {
        List <Button> buttons = new ArrayList <>();

        for (String s : this.gym.getWinAction().pokemonSpecList) {
            Pokemon p = this.gym.getWinAction().pokemonFromString(s);
            if (p != null) {
                GooeyButton button = GooeyButton.builder()
                        .title(Util.formattedString("&bPokemon Spec:"))
                        .lore(Util.formattedArrayList(Arrays.asList("&7" + s, "&4&lClick to remove")))
                        .display(SpriteItemHelper.getPhoto(p))
                        .onClick(b -> {
                            this.gym.getWinAction().pokemonSpecList.remove(s);
                            UIManager.openUIForcefully(b.getPlayer(), ManageSpecPage(player));
                        })
                        .build();
                buttons.add(button);
            }
        }

        return buttons;
    }


    public LinkedPage ManageSpecPage(Player player) {
        PlaceholderButton placeHolderButton = new PlaceholderButton();
        Template template = null;
        //back button

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player));
                })
                .build();


        template = ChestTemplate.builder(5)
                .border(0, 0, 5, 9, filler)
                .set(0, 0, back)
                .line(LineType.HORIZONTAL, 1, 1, 7, placeHolderButton)
                .build();

        return PaginationHelper.createPagesFromPlaceholders(template, pokemonSpecButtonList(player), LinkedPage.builder().title(Util.formattedString("&bGym Rewards")).template(template));
    }

    public GooeyPage ManagePokemonPage(Player player) {

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
                    UIManager.openUIForcefully(b.getPlayer(), ManageSpecPage(player));
                })
                .build();


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player));
                })
                .build();

        builder.set(2, 1, back);

        builder.set(2, 3, viewAndDeleteSpecs);

        builder.set(2, 5, addSpec);

        return GooeyPage.builder().template(builder.build()).build();
    }

    public GooeyPage EditActionPage(Player player) {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);

        int i = 0;
        for (GymWinActions action : GymWinActions.values()) {
            List <String> lore = new ArrayList <>();
            switch (action) {
                case Reward: {
                    lore.add("&aClick to view and modify what rewards are available");
                    break;
                }
                case Give_Money: {
                    lore.add("&aClick to set the amount of money given upon taking a clan gym");
                    lore.add("&7The current amount is: %amount%$".replace("%amount%", String.valueOf(this.gym.getWinAction().money)));
                    break;
                }
                case Give_Pokemon: {
                    lore.add("&aClick to modify what Pokemon should be given upon beating a gym");
                    break;
                }
                case Take_Pokemon: {
                    lore.add("&aClick to set whether a pokemon can be stolen from an npc upon beating a gym");
                    lore.add("&aResulting in them being stolen from the defeated clan");
                    String enabled = this.gym.getWinAction().takePokemon ? "&a&lEnabled" : "&c&ldisabled";
                    lore.add("&7This option is %status%".replace("%status%", enabled));
                    break;
                }
            }
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&b%action%".replace("%action%", action.name().replaceAll("_", " "))))
                    .display(getActionStack(action))
                    .lore(Util.formattedArrayList(lore))
                    .onClick(b -> {
                        switch (action) {
                            case Reward: {
                                UIManager.openUIForcefully(b.getPlayer(), RewardManageMenu(player));
                                break;
                            }
                            case Give_Money: {
                                dialogueInputScreenBuilder(GymBuilderAction.money_given, player).sendTo(player.getServerEntity());
                                break;
                            }
                            case Take_Pokemon: {

                                this.gym.getWinAction().updateTakeStatus();
                                //update gym cache? need to decide on a "finish" option
                                UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player));
                                break;
                            }
                            case Give_Pokemon: {
                                //open menu where current pokemon can be reviewed / added / removed
                                UIManager.openUIForcefully(b.getPlayer(), ManagePokemonPage(player));
                                // create args to give through specs
                                break;
                            }
                        }
                    })
                    .build();

            builder.set(2, i + 1, button);
            i++;
        }

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditGymPage(player));
                })
                .build();

        builder.set(0, 0, back);

        return GooeyPage.builder().template(builder.build()).build();
    }

    public List <Pokemon> pokemonList() {
        List <Pokemon> pokemonList = new ArrayList <>();
        for (Species sp : PixelmonSpecies.getAll()) {
            if (sp != null) {
                if (sp.equals(PixelmonSpecies.MISSINGNO.getValueUnsafe()))
                    continue;
                for (Stats st : sp.getForms()) {
                    Pokemon pokemon = PokemonBuilder.builder()
                            .species(sp)
                            .form(st)
                            .build();
                    pokemonList.add(pokemon);
                }

            }
        }
        return pokemonList;
    }

    public List<Button> pokemonButtonList()
    {
        List<Button> buttons = new ArrayList <>();
        for (String spec : this.gym.getHoldRequirements().bannedPokemonSpecs) {
            Pokemon p = PokemonSpecificationProxy.create(spec).create();
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("" + p.getSpecies().getName()))
                    .lore(Util.formattedArrayList(Arrays.asList("Form: " + p.getForm().getName())))
                    .display(SpriteItemHelper.getPhoto(p))
                    .build();
            buttons.add(button);
        }
        return buttons;
    }

    public ITechnicalMove returnITechnicalMoveFromImmutableAttack(ImmutableAttack attack)
    {
        ITechnicalMove iTechnicalMove = null;
        for (ITechnicalMove it : Clandorus.iTechnicalMoveList) {
            if (it.getAttack().getAttackName().equalsIgnoreCase(attack.getAttackName()))
                iTechnicalMove = it;
        }
        return iTechnicalMove;
    }


    public List<Button> attackButtonList(Player player)
    {
        List<Button> buttons = new ArrayList <>();
        List<ImmutableAttack> attacks = new ArrayList <>(AttackRegistry.getAllAttacks());
        attacks.sort(Comparator.comparing(ImmutableAttack::getAttackType));
        for (ImmutableAttack atk:attacks) {
            ItemStack stack = new ItemStack(PixelmonItems.tm_case);
            ITechnicalMove iTechnicalMove = returnITechnicalMoveFromImmutableAttack(atk);
            if (iTechnicalMove != null)
                stack = PixelmonItems.createTMStackFor(iTechnicalMove);
            //set enchanted if disabled
            String bannedStatus = "&a&lNot Banned";
            if (this.gym.getHoldRequirements().bannedMoves.contains(atk.getAttackName()))
                bannedStatus = "&4&lBanned";
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("" + atk.getAttackName()))
                    .lore(Util.formattedArrayList(Arrays.asList("&7Ban Status: %status%".replace("%status%", bannedStatus))))
                    .display(stack)
                    .onClick(b -> {
                        if (this.gym.getHoldRequirements().bannedMoves.contains(atk.getAttackName()))
                            this.gym.getHoldRequirements().bannedMoves.remove(atk.getAttackName());
                        else this.gym.getHoldRequirements().bannedMoves.add(atk.getAttackName());
                        UIManager.openUIForcefully(b.getPlayer(), ManageAttacksPage(player));
                    })
                    .build();
            buttons.add(button);
        }

        return buttons;
    }

    public List<Button> abilityButtonList(Player player)
    {
        List<Button> buttons = new ArrayList <>();

        for (Ability ab:Clandorus.abilityList) {
            String bannedStatus = "&a&lNot Banned";
            if (this.gym.getHoldRequirements().bannedAbilities.contains(ab.getName()))
                bannedStatus = "&4&lBanned";
            GooeyButton button = GooeyButton.builder()
                    .title(Util.formattedString("&b" + ab.getName()))
                    .lore(Util.formattedArrayList(Arrays.asList("&7Ban Status: %status%".replace("%status%", bannedStatus))))
                    .onClick(b -> {
                        if (this.gym.getHoldRequirements().bannedAbilities.contains(ab.getName()))
                            this.gym.getHoldRequirements().bannedAbilities.remove(ab.getName());
                        else this.gym.getHoldRequirements().bannedAbilities.add(ab.getName());
                        UIManager.openUIForcefully(b.getPlayer(), ManageAbilitiesPage(player));
                    })
                    .display(new ItemStack(PixelmonItems.ability_capsule))
                    .build();
            buttons.add(button);
        }


        return buttons;
    }

    public LinkedPage ManagePokemonSpecsPage(Player player) {
        PlaceholderButton placeHolderButton = new PlaceholderButton();

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageRequirementsPage(player));
                })
                .build();

        GooeyButton addSpec = GooeyButton.builder()
                .title(Util.formattedString("&cAdd Banned Spec"))
                .display(new ItemStack(PixelmonItems.pokemon_editor))
                .onClick(b -> {
                    dialogueInputScreenBuilder(GymBuilderAction.ban_spec, player).sendTo(b.getPlayer());
                })
                .build();

        GooeyButton banUltraBeast = GooeyButton.builder()

                .display(SpriteItemHelper.getPhoto(PokemonFactory.create(PixelmonSpecies.NIHILEGO.getValueUnsafe())))
                .title(Util.formattedString("&cBan Ultra Beasts"))
                .onClick(b -> {
                    this.gym.getHoldRequirements().banUBS();
                    UIManager.openUIForcefully(b.getPlayer(), ManagePokemonSpecsPage(player));
                })
                .build();

        GooeyButton banLegendaries = GooeyButton.builder()

                .display(SpriteItemHelper.getPhoto(PokemonFactory.create(PixelmonSpecies.MEW.getValueUnsafe())))
                .title(Util.formattedString("&cBan Legendary Pokemon"))
                .onClick(b -> {
                    this.gym.getHoldRequirements().banLegends();
                    UIManager.openUIForcefully(b.getPlayer(), ManagePokemonSpecsPage(player));
                })
                .build();

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

        if (pokemonButtonList().size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 4, back)
                    .set(0, 5, next)
                    .set(0, 0, addSpec)
                    .set(0, 1, banLegendaries)
                    .set(0, 2, banUltraBeast)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .set(0, 1, addSpec)
                    .set(0, 2, banLegendaries)
                    .set(0, 3, banUltraBeast)
                    .row(1, placeHolderButton)
                    .build();
        }

        //back button

        return PaginationHelper.createPagesFromPlaceholders(template, pokemonButtonList(), LinkedPage.builder().title(Util.formattedString("&bBanned Pokemon")).template(template));
    }

    public LinkedPage ManageAttacksPage(Player player) {
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


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageRequirementsPage(player));
                })
                .build();

        Template template;

        if (attackButtonList(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 0, back)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, attackButtonList(player), LinkedPage.builder().title(Util.formattedString("&bBanned Attacks")).template(template));
    }

    public LinkedPage ManageAbilitiesPage(Player player) {
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


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageRequirementsPage(player));
                })
                .build();

        Template template;

        if (abilityButtonList(player).size() > 8) {
            template = ChestTemplate.builder(6)
                    .border(0, 0, 6, 9, filler)
                    .set(0, 3, previous)
                    .set(0, 0, back)
                    .set(0, 5, next)
                    .rectangle(1, 1, 4, 7, placeHolderButton)
                    .build();
        } else {
            template = ChestTemplate.builder(3)
                    .border(0, 0, 3, 9, filler)
                    .set(0, 0, back)
                    .row(1, placeHolderButton)
                    .build();
        }

        return PaginationHelper.createPagesFromPlaceholders(template, abilityButtonList(player), LinkedPage.builder().title(Util.formattedString("&bBanned Attacks")).template(template));
    }

    public GooeyPage IVSManagePage(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);

        GooeyButton increase = GooeyButton.builder()
                .title(Util.formattedString("&aIncrease"))
                .display(new ItemStack(Items.DIAMOND))
                .lore(Util.formattedArrayList(Arrays.asList("&eClick to increase the minimum amount of ivs that have reached 31 are needed for a pokemon to be legal.", "&7Currently at: %amount%".replace("%amount%", String.valueOf(this.gym.getHoldRequirements().min31Ivs)))))
                .onClick(b -> {
                    if (this.gym.getHoldRequirements().min31Ivs >= 6)
                        return;
                    this.gym.getHoldRequirements().min31Ivs += 1;
                    UIManager.openUIForcefully(b.getPlayer(), IVSManagePage(player));
                })
                .build();

        GooeyButton decrease = GooeyButton.builder()
                .title(Util.formattedString("&cDecrease"))
                .lore(Util.formattedArrayList(Arrays.asList("&eClick to decrease the minimum amount of ivs that have reached 31 are needed for a pokemon to be legal.", "&7Currently at: %amount%".replace("%amount%", String.valueOf(this.gym.getHoldRequirements().min31Ivs)))))
                .display(new ItemStack(Items.COAL))
                .onClick(b -> {
                    if (this.gym.getHoldRequirements().min31Ivs <= 0)
                        return;
                    this.gym.getHoldRequirements().min31Ivs -= 1;
                    UIManager.openUIForcefully(b.getPlayer(), IVSManagePage(player));
                })
                .build();

        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageRequirementsPage(player));
                })
                .build();

        builder.set(1, 2, decrease);
        builder.set(1, 4, back);
        builder.set(1, 6, increase);

        return GooeyPage.builder().template(builder.build()).build();
    }


    public GooeyPage ManageRequirementsPage(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        builder.border(0, 0, 6, 9, filler);

        GooeyButton ivsButton = GooeyButton.builder()
                .title(Util.formattedString("&aManage the minimum ivs"))
                .display(new ItemStack(PixelmonItems.destiny_knot))
                .onClick(b ->
                {
                    // open menu with a decrease and increase option with a min of 0 and max of 6
                    UIManager.openUIForcefully(b.getPlayer(), IVSManagePage(player));
                })
                .build();

        GooeyButton bannedAbilities = GooeyButton.builder()
                .title(Util.formattedString("&eBanned Abilities"))
                .display(new ItemStack(PixelmonItems.ability_patch))
                .onClick(b -> {
                    // open menu with scrollable abilities to ban
                    UIManager.openUIForcefully(b.getPlayer(), ManageAbilitiesPage(player));
                })
                .build();

        GooeyButton bannedMoves = GooeyButton.builder()
                .title(Util.formattedString("&6Banned Moves"))
                .display(new ItemStack(PixelmonItems.tm_case))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageAttacksPage(player));
                })
                .build();

        GooeyButton bannedPokemon = GooeyButton.builder()
                .title(Util.formattedString("&9Banned Pokemon"))
                .display(SpriteItemHelper.getPhoto(PokemonBuilder.builder().species(PixelmonSpecies.getRandomSpecies()).build()))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManagePokemonSpecsPage(player));
                })
                .build();


        GooeyButton back = GooeyButton.builder()
                .title(Util.formattedString("&eGo Back"))
                .display(new ItemStack(PixelmonItems.eject_button))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditGymPage(player));
                })
                .build();


        builder.set(2, 1, ivsButton);
        builder.set(2, 3, bannedAbilities);
        builder.set(2, 5, bannedMoves);
        builder.set(2, 7, bannedPokemon);
        builder.set(0, 0, back);

        return GooeyPage.builder().template(builder.build()).build();
    }


    public GooeyPage EditGymPage(Player player)
    {
        ChestTemplate.Builder builder = ChestTemplate.builder(6);

        GooeyButton holder = GooeyButton.builder()
                .title(Util.formattedString("&7Current Holder: %holder%".replace("%holder%", this.gym.getHoldingPlayerName())))
                .display(new ItemStack(Items.SKELETON_SKULL))
                .build();

        GooeyButton defaultTeam = GooeyButton.builder()
                .title(Util.formattedString("&7Default Team: %team%".replace("%team%", this.gym.getDefaultTeamID())))
                .display(PokeBallRegistry.CHERISH_BALL.getValue().get().getBallItem())
                .onClick(b -> {
                    //go to selection page
                })
                .build();

        GooeyButton activeTeam = GooeyButton.builder()
                .title(Util.formattedString("&aActive team: "))
                .display(new ItemStack(PixelmonItems.poke_ball))
                .lore(Util.formattedArrayList(getCurrentPokemonLore(this.npcTrainer)))
                .build();

        GooeyButton gymActions = GooeyButton.builder()
                .title(Util.formattedString("&eGym Actions"))
                .display(new ItemStack(Items.LEAD))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), EditActionPage(player));
                })
                .build();

        GooeyButton holdRequirements = GooeyButton.builder()
                .title(Util.formattedString("&6Gym Rules and Requirements"))
                .display(new ItemStack(PixelmonItems.pokemon_editor))
                .onClick(b -> {
                    UIManager.openUIForcefully(b.getPlayer(), ManageRequirementsPage(player));
                })
                .build();


        GooeyButton save = GooeyButton.builder()
                .title(Util.formattedString("&aSave"))
                .onClick(b -> {
                    this.gym.setGymEntityID(npcTrainer.getEntityId());
                    npcTrainer.getPersistentData().putBoolean("clandorusGym", true);
                    npcTrainer.getPersistentData().putString("clandorusGymID", gym.getIdentifier());
                    this.gym.save();
                    UIManager.closeUI(b.getPlayer());
                })
                .display(new ItemStack(PixelmonItems.up_grade))
                .build();


        builder.border(0, 0, 6, 9, filler);


        builder.set(1, 1, holder);
        builder.set(1, 3, defaultTeam);
        builder.set(1, 5, activeTeam);
        builder.set(1, 7, gymActions);
        builder.set(2, 4, holdRequirements);
        builder.set(3, 4, save);

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
